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
package com.wafitz.pixelspacebase.ui;

import com.wafitz.pixelspacebase.Assets;
import com.wafitz.pixelspacebase.Dungeon;
import com.wafitz.pixelspacebase.actors.Char;
import com.wafitz.pixelspacebase.actors.buffs.Buff;
import com.wafitz.pixelspacebase.scenes.GameScene;
import com.wafitz.pixelspacebase.windows.WndInfoBuff;
import com.watabou.gltextures.SmartTexture;
import com.watabou.gltextures.TextureCache;
import com.watabou.noosa.Image;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.tweeners.AlphaTweener;
import com.watabou.noosa.ui.Button;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.SparseArray;

public class BuffIndicator extends Component {

    public static final int NONE = -1;

    //TODO consider creating an enum to store both index, and tint. Saves making separate images for color differences.
    public static final int INTRUDER_ALERT = 0;
    public static final int JETPACK = 1;
    public static final int FIRE = 2;
    public static final int POISON = 3;
    public static final int PARALYSIS = 4;
    public static final int HUNGER = 5;
    public static final int STARVATION = 6;
    public static final int SLOW = 7;
    public static final int OOZE = 8;
    public static final int AMOK = 9;
    public static final int TERROR = 10;
    public static final int LOCKDOWN = 11;
    public static final int CAMOFLAGE = 12;
    public static final int CAMOFLAGED = 13;
    public static final int WEAKNESS = 14;
    public static final int FROST = 15;
    public static final int BLINDNESS = 16;
    public static final int COMBO = 17;
    public static final int FURY = 18;
    public static final int HEALING = 19;
    public static final int WEAKFORCEFIELD = 20;
    public static final int HYPNOTISE = 21;
    public static final int LIGHT = 22;
    public static final int CRIPPLE = 23;
    public static final int SHIELDING = 24;
    public static final int IMMUNITY = 25;
    public static final int BLEEDING = 26;
    public static final int TARGETED = 27;
    public static final int DEFERRED = 28;
    public static final int KNOCKOUT = 29;
    public static final int TIRED = 30;
    public static final int STRONGFORCEFIELD = 31;
    public static final int SURVEY = 32;
    public static final int VERTIGO = 33;
    public static final int RECHARGING = 34;
    public static final int LOCKED_FLOOR = 35;
    public static final int DOMINATION = 36;
    public static final int UPGRADE = 37;
    public static final int RAGE = 38;
    public static final int SACRIFICE = 39;
    public static final int ANGERED = 40;
    public static final int EXHAUSTED = 41;
    public static final int RECOVERING = 42;

    public static final int SIZE = 7;

    private static BuffIndicator heroInstance;

    private SmartTexture texture;
    private TextureFilm film;

    private SparseArray<BuffIcon> icons = new SparseArray<>();

    private Char ch;

    public BuffIndicator(Char ch) {
        super();

        this.ch = ch;
        if (ch == Dungeon.hero) {
            heroInstance = this;
        }
    }

    @Override
    public void destroy() {
        super.destroy();

        if (this == heroInstance) {
            heroInstance = null;
        }
    }

    @Override
    protected void createChildren() {
        texture = TextureCache.get(Assets.BUFFS_SMALL);
        film = new TextureFilm(texture, SIZE, SIZE);
    }

    @Override
    protected void layout() {
        clear();

        SparseArray<BuffIcon> newIcons = new SparseArray<>();

        for (Buff buff : ch.buffs()) {
            if (buff.icon() != NONE) {
                BuffIcon icon = new BuffIcon(buff);
                icon.setRect(x + members.size() * (SIZE + 2), y, 9, 12);
                add(icon);
                newIcons.put(buff.icon(), icon);
            }
        }

        for (Integer key : icons.keyArray()) {
            if (newIcons.get(key) == null) {
                Image icon = icons.get(key).icon;
                icon.origin.set(SIZE / 2);
                add(icon);
                add(new AlphaTweener(icon, 0, 0.6f) {
                    @Override
                    protected void updateValues(float progress) {
                        super.updateValues(progress);
                        image.scale.set(1 + 5 * progress);
                    }

                    @Override
                    protected void onComplete() {
                        image.killAndErase();
                    }
                });
            }
        }

        icons = newIcons;
    }

    private class BuffIcon extends Button {

        private Buff buff;

        public Image icon;

        public BuffIcon(Buff buff) {
            super();
            this.buff = buff;

            icon = new Image(texture);
            icon.frame(film.get(buff.icon()));
            add(icon);
        }

        @Override
        protected void layout() {
            super.layout();
            icon.x = this.x + 1;
            icon.y = this.y + 2;
        }

        @Override
        protected void onClick() {
            if (buff.icon() != NONE)
                GameScene.show(new WndInfoBuff(buff));
        }
    }

    public static void refreshHero() {
        if (heroInstance != null) {
            heroInstance.layout();
        }
    }
}