/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015  Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2016 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.wafitz.pixelspacebase.items.armor.enhancements;

import com.wafitz.pixelspacebase.actors.Char;
import com.wafitz.pixelspacebase.actors.buffs.Buff;
import com.wafitz.pixelspacebase.actors.buffs.Hypnotise;
import com.wafitz.pixelspacebase.effects.Speck;
import com.wafitz.pixelspacebase.items.armor.Armor;
import com.wafitz.pixelspacebase.sprites.ItemSprite;
import com.wafitz.pixelspacebase.sprites.ItemSprite.Glowing;
import com.watabou.utils.Random;

public class Hypnosis extends Armor.Enhancement {

    private static ItemSprite.Glowing PINK = new ItemSprite.Glowing(0xFF4488);

    @Override
    public int proc(Armor armor, Char attacker, Char defender, int damage) {

        int level = Math.max(0, armor.level());

        if (Random.Int(level / 2 + 10) >= 9) {

            int duration = Random.IntRange(2, 5);

            Buff.affect(attacker, Hypnotise.class, Hypnotise.durationFactor(attacker) * duration).object = defender.id();
            attacker.sprite.centerEmitter().start(Speck.factory(Speck.HEART), 0.2f, 5);

        }

        return damage;
    }

    @Override
    public Glowing glowing() {
        return PINK;
    }
}