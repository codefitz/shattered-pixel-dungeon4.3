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
package com.wafitz.pixelspacebase.items.scripts;

import com.wafitz.pixelspacebase.Assets;
import com.wafitz.pixelspacebase.actors.buffs.Camoflage;
import com.wafitz.pixelspacebase.items.Item;
import com.wafitz.pixelspacebase.messages.Messages;
import com.wafitz.pixelspacebase.scenes.GameScene;
import com.wafitz.pixelspacebase.windows.WndContainer;
import com.watabou.noosa.audio.Sample;

abstract class InventoryScript extends Script {

    private String inventoryTitle = Messages.get(this, "inv_title");
    protected WndContainer.Mode mode = WndContainer.Mode.ALL;

    @Override
    protected void doRead() {

        if (!isKnown()) {
            setKnown();
            identifiedByUse = true;
        } else {
            identifiedByUse = false;
        }

        GameScene.selectItem(itemSelector, mode, inventoryTitle);
    }

    // wafitz.v5: Probably I will put this back in but with a non-optional "Script identifies itself"
    /*private void confirmCancelation() {
        GameScene.show(new WndOptions(name(), Messages.get(this, "warning"),
                Messages.get(this, "yes"), Messages.get(this, "no")) {
            @Override
            protected void onSelect(int index) {
                switch (index) {
                    case 0:
                        curUser.spendAndNext(TIME_TO_READ);
                        identifiedByUse = false;
                        break;
                    case 1:
                        GameScene.selectItem(itemSelector, mode, inventoryTitle);
                        break;
                }
            }

            public void onBackPressed() {
            }
        });
    }*/

    protected abstract void onItemSelected(Item item);

    private static boolean identifiedByUse = false;
    protected static WndContainer.Listener itemSelector = new WndContainer.Listener() {
        @Override
        public void onSelect(Item item) {
            if (item != null) {

                ((InventoryScript) curItem).onItemSelected(item);
                ((InventoryScript) curItem).readAnimation();

                Sample.INSTANCE.play(Assets.SND_READ);
                Camoflage.dispel();

                // wafitz.v1 - Lost count how many times I lost this script accidentally - no more!
                //} else if (identifiedByUse && !((Script) curItem).ownedByBook) {

                //((InventoryScript) curItem).confirmCancelation();

            } else if (!((Script) curItem).ownedByBook) {

                curItem.collect(curUser.belongings.backpack);

            }
        }
    };
}
