/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015-2016, Max Roncace <me@caseif.net>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package net.caseif.flint.common.lobby.populator;

import net.caseif.flint.lobby.LobbySign;
import net.caseif.flint.lobby.populator.LobbySignPopulator;

import com.google.common.base.Function;

public class FunctionalLobbySignPopulator implements LobbySignPopulator {

    private final Function<LobbySign, String> first;
    private final Function<LobbySign, String> second;
    private final Function<LobbySign, String> third;
    private final Function<LobbySign, String> fourth;

    private FunctionalLobbySignPopulator(Function<LobbySign, String> first, Function<LobbySign, String> second,
                                         Function<LobbySign, String> third, Function<LobbySign, String> fourth) {
        this.first = first;
        this.second = second;
        this.third = third;
        this.fourth = fourth;
    }

    @Override
    public String first(LobbySign sign) {
        return first.apply(sign);
    }

    @Override
    public String second(LobbySign sign) {
        return second.apply(sign);
    }

    @Override
    public String third(LobbySign sign) {
        return third.apply(sign);
    }

    @Override
    public String fourth(LobbySign sign) {
        return fourth.apply(sign);
    }

    private static class Builder implements LobbySignPopulator.Builder {

        private static final Function<LobbySign, String> NOOP = new Function<LobbySign, String>() {
            @Override
            public String apply(LobbySign lobbySign) {
                return "";
            }
        };

        private Function<LobbySign, String> first;
        private Function<LobbySign, String> second;
        private Function<LobbySign, String> third;
        private Function<LobbySign, String> fourth;

        @Override
        public LobbySignPopulator.Builder first(Function<LobbySign, String> function) {
            this.first = function;
            return this;
        }

        @Override
        public LobbySignPopulator.Builder second(Function<LobbySign, String> function) {
            this.second = function;
            return this;
        }

        @Override
        public LobbySignPopulator.Builder third(Function<LobbySign, String> function) {
            this.third = function;
            return this;
        }

        @Override
        public LobbySignPopulator.Builder fourth(Function<LobbySign, String> function) {
            this.fourth = function;
            return this;
        }

        @Override
        public LobbySignPopulator build() {
            return new FunctionalLobbySignPopulator(first != null ? first : NOOP, second != null ? second : NOOP,
                    third != null ? third : NOOP, fourth != null ? fourth : NOOP);
        }

    }

}
