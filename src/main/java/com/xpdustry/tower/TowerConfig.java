/*
 * This file is part of TowerDefense. An implementation of the tower defense gamemode by Xpdustry.
 *
 * MIT License
 *
 * Copyright (c) 2024 Xpdustry
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.xpdustry.tower;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import mindustry.type.UnitType;
import mindustry.world.Block;
import org.github.gestalt.config.annotations.Config;

public record TowerConfig(
        float healthMultiplier,
        int mitosis,
        @Config(path = "unit-bind") boolean ubind,
        Set<Block> blockWhitelist,
        Map<String, List<TowerDrop>> drops,
        Map<UnitType, UnitData> units) {
    public TowerConfig {
        if (healthMultiplier < 1F) {
            throw new IllegalArgumentException("health-multiplier can't be lower than 1");
        }
        for (final var data : units.values()) {
            if (!drops.containsKey(data.drop)) {
                throw new IllegalArgumentException("drops do not exist: " + data.drop);
            }
        }
    }

    public record UnitData(String drop, Optional<UnitType> downgrade) {}
}
