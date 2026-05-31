package de.dasbabypixel.gamelauncher.api.util.logging

object PatternParser {
    const val BEGIN_FORMAT = '%'
    const val BEGIN_FORMAT_CONTENT = '{'
    const val END_FORMAT_CONTENT = '}'
    const val END_FORMAT = '$'

    fun parse(input: String): ParseResult {
        // All the root level results
        val state = State()
        state.pushParser(Parser.Text())
        try {
            for (char in input) {
                state.parserState.parse(state, char)
                state.cursor++
            }

            while (state.parserStates.size > 0) {
                state.parserState.tryEnd(state)
            }

            return state.results.build
        } catch (e: PatternException) {
            throw PatternException(e, " at cursor ${state.cursor} for \"$input\"")
        }
    }

    private val List<ParseResult>.build: ParseResult
        get() = if (size == 1) this[0] else if (isEmpty()) ParseResult.Empty else ParseResult.Multi(
            this
        )

    private sealed interface Parser {
        fun parse(state: State, char: Char)

        fun end(state: State)

        fun tryEnd(state: State)

        class Format : Parser {
            companion object {
                const val STATE_NAME = 0
                const val STATE_NAMING = 1
                const val STATE_BEGIN_CONTENT = 2
                const val STATE_END_CONTENT = 3
                const val STATE_BEGIN_OPTIONS = 4
                const val STATE_END_OPTIONS = 5
            }

            val type = StringBuilder()
            var options: MutableList<ParseResult>? = null
            var results: MutableList<ParseResult>? = null
            var state = STATE_NAME

            val optionsSafe: MutableList<ParseResult>
                get() = options ?: ArrayList<ParseResult>().apply { options = this }
            val resultsSafe: MutableList<ParseResult>
                get() = results ?: ArrayList<ParseResult>().apply { results = this }

            override fun parse(state: State, char: Char) {
                when (this.state) {
                    STATE_NAME -> when (char) {
                        BEGIN_FORMAT -> throw PatternException("Can't begin format during format name")
                        BEGIN_FORMAT_CONTENT -> throw PatternException("Can't begin format content without naming the format")
                        END_FORMAT_CONTENT -> throw PatternException("Can't end format content without naming the format")
                        else -> {
                            type.append(char)
                            this.state = STATE_NAMING
                        }
                    }

                    STATE_NAMING -> when (char) {
                        BEGIN_FORMAT -> { // Beginning another format, or literal text
                            end(state)
                            state.pushParser(Text())
                            state.parserState.parse(state, char)
                        }

                        BEGIN_FORMAT_CONTENT -> {
                            this.state = STATE_BEGIN_CONTENT
                            resultsSafe
                            state.pushAppender { resultsSafe.add(it) }
                            state.pushParser(Text())
                        }

                        END_FORMAT_CONTENT -> { // Simple format, no content, no options. End next format content
                            end(state)
                            state.parserState.parse(state, char)
                        }

                        END_FORMAT -> {
                            end(state)
                            state.parserState.parse(state, char)
                        }

                        else -> {
                            if ((char < 'a' || char > 'z') && char != '_') { // Format name terminated by invalid character
                                end(state)
                                state.parserState.parse(state, char)
                            } else type.append(char)
                        }
                    }

                    STATE_BEGIN_CONTENT -> when (char) {
                        BEGIN_FORMAT -> throw PatternException("Pattern error: Shouldn't be possible")
                        BEGIN_FORMAT_CONTENT -> throw PatternException("Pattern error: Shouldn't be possible")
                        END_FORMAT_CONTENT -> {
                            this.state = STATE_END_CONTENT
                            state.popAppender
                        }
                    }

                    STATE_END_CONTENT -> when (char) {
                        BEGIN_FORMAT -> { // Beginning another format, or literal text
                            end(state)
                            state.pushParser(Text())
                            state.parserState.parse(state, char)
                        }

                        BEGIN_FORMAT_CONTENT -> {
                            this.state = STATE_BEGIN_OPTIONS
                            optionsSafe
                            state.pushAppender { optionsSafe.add(it) }
                            state.pushParser(Text())
                        }

                        END_FORMAT_CONTENT -> { // Format with content but no options. End next format content
                            end(state)
                            state.parserState.parse(state, char)
                        }

                        else -> { // Keep on typing illegal character - probably text
                            end(state)
                            state.parserState.parse(state, char)
                        }
                    }

                    STATE_BEGIN_OPTIONS -> when (char) {
                        BEGIN_FORMAT -> throw PatternException("Pattern error: Shouldn't be possible")
                        BEGIN_FORMAT_CONTENT -> throw PatternException("Pattern error: Shouldn't be possible")
                        END_FORMAT_CONTENT -> {
                            this.state = STATE_END_OPTIONS
                            state.popAppender
                            end(state)
                        }
                    }

                    STATE_END_OPTIONS -> throw PatternException("Pattern error: Shouldn't be possible")
                }
            }

            override fun end(state: State) {
                val p = state.popParser
                if (p != this) throw PatternException("State parser mismatch: Expected Format, found ${p::class.simpleName}")
                val type = PatternRegistry.pattern(this.type.toString())
                state.pushParser(Text())
                state.appender(ParseResult.Formatted(type, results?.build, options?.build))
            }

            override fun tryEnd(state: State) {
                if (this.state == STATE_NAMING || this.state == STATE_END_CONTENT) {
                    end(state)
                } else throw PatternException("Invalid state for tryEnd: ${this.state}")
            }
        }

        class Text : Parser {
            companion object {
                const val STATE_TEXT = 0
                const val STATE_FORMAT = 1
            }

            val text = StringBuilder()
            var state = STATE_TEXT
            override fun parse(state: State, char: Char) {
                when (this.state) {
                    STATE_TEXT -> when (char) {
                        BEGIN_FORMAT -> this.state = STATE_FORMAT
                        END_FORMAT -> throw PatternException("End format ($END_FORMAT) is not allowed here without escaping")
                        BEGIN_FORMAT_CONTENT -> throw PatternException("Begin format ($BEGIN_FORMAT_CONTENT) not allowed here without escaping")
                        END_FORMAT_CONTENT -> {
                            end(state)
                            state.parserState.parse(state, char)
                        }

                        else -> text.append(char)
                    }

                    STATE_FORMAT -> when (char) {
                        BEGIN_FORMAT, END_FORMAT, BEGIN_FORMAT_CONTENT, END_FORMAT_CONTENT -> {
                            text.append(char)
                            this.state = STATE_TEXT
                        }

                        else -> {
                            end(state, Format())
                            state.parserState.parse(state, char)
                        }
                    }
                }
            }

            override fun end(state: State) {
                end(state, null)
            }

            override fun tryEnd(state: State) {
                if (this.state == STATE_FORMAT) throw PatternException("Can't end in format")
                end(state)
            }

            fun end(state: State, newParser: Parser?) {
                val p = state.popParser
                if (p != this) throw PatternException("State parser mismatch: Expected Text, found ${p::class.simpleName}")
                if (newParser != null) state.pushParser(newParser)
                if (text.isEmpty()) return
                state.appender(ParseResult.Text(text.toString()))
            }
        }
    }

    private class State {
        val results = ArrayList<ParseResult>()
        var parserStateUnsafe: Parser? = null
        val parserState: Parser
            get() {
                if (parserStateUnsafe == null) {
                    Thread.dumpStack()
                    pushParser(Parser.Text())
                }
                return parserStateUnsafe!!
            }
        val parserStates: MutableList<Parser> = ArrayList()
        var appender: (ParseResult) -> Unit = { results.add(it) }
        val appenders: MutableList<(ParseResult) -> Unit> = arrayListOf(appender)
        var cursor = 0

        val popParser: Parser
            get() {
                val parser = parserStates.removeAt(parserStates.lastIndex)
                parserStateUnsafe = parserStates.lastOrNull()
                return parser
            }

        fun pushParser(parser: Parser) {
            parserStates.add(parser)
            parserStateUnsafe = parser
        }

        val popAppender: (ParseResult) -> Unit
            get() {
                if (appenders.size == 1) throw PatternException("Invalid pattern format")
                val appender = appenders.removeAt(appenders.lastIndex)
                this.appender = appenders.last()
                return appender
            }

        fun pushAppender(appender: (ParseResult) -> Unit) {
            appenders.add(appender)
            this.appender = appender
        }
    }
}
