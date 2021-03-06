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
package com.wafitz.pixelspacebase.items;

import com.wafitz.pixelspacebase.Assets;
import com.wafitz.pixelspacebase.actors.hero.Hero;
import com.wafitz.pixelspacebase.effects.CellEmitter;
import com.wafitz.pixelspacebase.effects.Speck;
import com.wafitz.pixelspacebase.messages.Messages;
import com.wafitz.pixelspacebase.sprites.ItemSprite.Glowing;
import com.wafitz.pixelspacebase.sprites.ItemSpriteSheet;
import com.wafitz.pixelspacebase.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;

import java.util.ArrayList;

public class Clone extends Item {

    private static final String AC_UPGRADE = "UPGRADE";

    {
        image = ItemSpriteSheet.CLONE;

        //You tell the ankh no, don't revive me, and then it comes back to revive you again in another run.
        //I'm not sure if that's enthusiasm or passive-aggression.
        bones = true;
    }

    private Boolean upgraded = false;

    @Override
    public boolean isUpgradable() {
        return false;
    }

    @Override
    public boolean isIdentified() {
        return true;
    }

    @Override
    public ArrayList<String> actions(Hero hero) {
        ArrayList<String> actions = super.actions(hero);
        AirTank vial = hero.belongings.getItem(AirTank.class);
        if (vial != null && vial.isFull() && !upgraded)
            actions.add(AC_UPGRADE);
        return actions;
    }

    @Override
    public void execute(final Hero hero, String action) {

        super.execute(hero, action);

        if (action.equals(AC_UPGRADE)) {

            AirTank vial = hero.belongings.getItem(AirTank.class);
            if (vial != null) {
                upgraded = true;
                vial.empty();
                GLog.p(Messages.get(this, "upgrade"));
                hero.spend(1f);
                hero.busy();


                Sample.INSTANCE.play(Assets.SND_DRINK);
                CellEmitter.get(hero.pos).start(Speck.factory(Speck.LIGHT), 0.2f, 3);
                hero.sprite.operate(hero.pos);
            }
        }
    }

    @Override
    public String desc() {
        if (upgraded)
            return Messages.get(this, "desc_upgraded");
        else
            return super.desc();
    }

    public Boolean isBlessed() {
        return upgraded;
    }

    private static final Glowing WHITE = new Glowing(0xFFFFCC);

    @Override
    public Glowing glowing() {
        return isBlessed() ? WHITE : null;
    }

    private static final String UPGRADED = "upgraded";

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(UPGRADED, upgraded);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        upgraded = bundle.getBoolean(UPGRADED);
    }

    @Override
    public int cost() {
        return 50 * quantity;
    }
}
