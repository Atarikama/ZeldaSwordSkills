/**
    Copyright (C) <2015> <coolAlias>

    This file is part of coolAlias' Zelda Sword Skills Minecraft Mod; as such,
    you can redistribute it and/or modify it under the terms of the GNU
    General Public License as published by the Free Software Foundation,
    either version 3 of the License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package zeldaswordskills.item;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.INpc;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.village.MerchantRecipe;
import zeldaswordskills.ZSSAchievements;
import zeldaswordskills.api.item.IUnenchantable;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.entity.ZSSPlayerInfo;
import zeldaswordskills.entity.ZSSPlayerSkills;
import zeldaswordskills.entity.ZSSVillagerInfo;
import zeldaswordskills.entity.npc.EntityNpcMaskTrader;
import zeldaswordskills.entity.npc.EntityNpcOrca;
import zeldaswordskills.ref.ModInfo;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.util.MerchantRecipeHelper;
import zeldaswordskills.util.PlayerUtils;
import zeldaswordskills.util.TimedChatDialogue;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 
 * Rare items with no use other than as potential trades for upgrades
 *
 */
public class ItemTreasure extends Item implements IUnenchantable
{
	/** All the different treasure types */
	public static enum Treasures {
		CLAIM_CHECK("claim_check"),
		COJIRO("cojiro"),
		EVIL_CRYSTAL("evil_crystal"),
		EYE_DROPS("eye_drops"),
		EYEBALL_FROG("eyeball_frog"),
		GORON_SWORD("goron_sword"),
		JELLY_BLOB("jelly_blob",true,32),
		MONSTER_CLAW("monster_claw",true,24),
		ODD_MUSHROOM("odd_mushroom"),
		ODD_POTION("odd_potion"),
		POACHER_SAW("poacher_saw"),
		POCKET_EGG("pocket_egg"),
		PRESCRIPTION("prescription"),
		TENTACLE("tentacle",true,16),
		ZELDAS_LETTER("zeldas_letter"),
		KNIGHTS_CREST("knights_crest","knights_crest",true,32);

		public final String name;
		/** Unlocalized string used to retrieve chat comment when an NPC is not interested in trading */
		public final String uninterested;
		private final boolean canSell;
		private final int value;

		private Treasures(String name) {
			this(name, "default", false, 0);
		}

		private Treasures(String name, boolean canSell, int value) {
			this(name, "default", canSell, value);
		}

		private Treasures(String name, String uninterested, boolean canSell, int value) {
			this.name = name;
			this.uninterested = uninterested;
			this.canSell = canSell;
			// this.value = value;
			// TODO there is a vanilla bug that prevents distinguishing between subtypes for the items to buy
			this.value = 24;
		}
		/** Whether this treasure is salable (currently used only for monster parts) */
		public boolean canSell() { return canSell; }
		/** The price at which the hunter will buy this treasure */
		public int getValue() { return value; }
	};

	@SideOnly(Side.CLIENT)
	private IIcon[] iconArray;

	public ItemTreasure() {
		super();
		setMaxDamage(0);
		setMaxStackSize(1);
		setHasSubtypes(true);
		setCreativeTab(ZSSCreativeTabs.tabMisc);
	}

	@Override
	public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
		if (!player.worldObj.isRemote) {
			Treasures treasure = Treasures.values()[stack.getItemDamage() % Treasures.values().length];
			if (entity instanceof EntityVillager) {
				EntityVillager villager = (EntityVillager) entity;
				ZSSVillagerInfo villagerInfo = ZSSVillagerInfo.get(villager);
				MerchantRecipe trade = ZSSVillagerInfo.getTreasureTrade(treasure);
				boolean isBaseVillager = entity.getClass().isAssignableFrom(EntityVillager.class);
				villager.playLivingSound();
				if (treasure == Treasures.KNIGHTS_CREST && isBaseVillager && villager.getCustomNameTag().equals("Orca")) {
					EntityNpcOrca orca = new EntityNpcOrca(villager.worldObj);
					orca.setLocationAndAngles(villager.posX, villager.posY, villager.posZ, villager.rotationYaw, villager.rotationPitch);
					orca.setCustomNameTag(villager.getCustomNameTag());
					if (!orca.worldObj.isRemote) {
						orca.worldObj.spawnEntityInWorld(orca);
					}
					villager.setDead();
					PlayerUtils.playSound(player, Sounds.SUCCESS, 1.0F, 1.0F);
					ZSSPlayerSkills.get(player).giveCrest();
				} else if (treasure == Treasures.ZELDAS_LETTER) {
					if (isBaseVillager && villager.getCustomNameTag().contains("Mask Salesman")) {
						EntityNpcMaskTrader trader = new EntityNpcMaskTrader(villager.worldObj);
						trader.setLocationAndAngles(villager.posX, villager.posY, villager.posZ, villager.rotationYaw, villager.rotationPitch);
						trader.setCustomNameTag(villager.getCustomNameTag());
						if (!trader.worldObj.isRemote) {
							trader.worldObj.spawnEntityInWorld(trader);
						}
						villager.setDead();
						PlayerUtils.playSound(player, Sounds.SUCCESS, 1.0F, 1.0F);
						player.triggerAchievement(ZSSAchievements.maskTrader);
						if (ZSSPlayerInfo.get(player).getCurrentMaskStage() == 0) {
							List<String> chat = new ArrayList<String>(5);
							for (int i = 0; i < 5; ++i) {
								chat.add(StatCollector.translateToLocal("chat.zss.treasure." + treasure.name + ".success." + i));
							}
							new TimedChatDialogue(player, chat);
						} else {
							PlayerUtils.sendTranslatedChat(player, "chat.zss.treasure." + treasure.name + ".already_open");
						}
						player.setCurrentItemOrArmor(0, null);
					} else {
						PlayerUtils.sendTranslatedChat(player, "chat.zss.treasure." + treasure.name + ".fail");
					}
				} else if (trade != null && villagerInfo.isInterested(treasure, stack)) {
					ItemStack required = trade.getSecondItemToBuy();
					if (required == null || PlayerUtils.consumeInventoryItem(player, required, required.stackSize)) {
						PlayerUtils.playSound(player, Sounds.SUCCESS, 1.0F, 1.0F);
						player.setCurrentItemOrArmor(0, trade.getItemToSell());
						PlayerUtils.sendTranslatedChat(player, "chat." + getUnlocalizedName(stack).substring(5) + ".give");
						PlayerUtils.sendFormattedChat(player, "chat.zss.treasure.received", trade.getItemToSell().getDisplayName());
						if (villagerInfo.onTradedTreasure(player, treasure, player.getHeldItem())) {
							PlayerUtils.sendTranslatedChat(player, "chat." + getUnlocalizedName(stack).substring(5) + ".next");
						}
					} else {
						PlayerUtils.sendFormattedChat(player, "chat.zss.treasure.trade.fail", required.stackSize, required.getDisplayName(), (required.stackSize > 1 ? "s" : ""));
					}
				} else if (treasure.canSell() && villagerInfo.isHunter()) {
					ItemStack treasureStack = new ItemStack(ZSSItems.treasure,1,treasure.ordinal());
					int price = villagerInfo.isMonsterHunter() ? treasure.getValue() + treasure.getValue() / 2 : treasure.getValue();
					if (MerchantRecipeHelper.addToListWithCheck(villager.getRecipes(player), new MerchantRecipe(treasureStack, new ItemStack(Items.emerald, price)))) {
						PlayerUtils.playSound(player, Sounds.SUCCESS, 1.0F, 1.0F);
						PlayerUtils.sendFormattedChat(player, "chat.zss.treasure.hunter.new", treasureStack.getDisplayName());
					} else {
						PlayerUtils.sendFormattedChat(player, "chat.zss.treasure.hunter.old", treasureStack.getDisplayName());
					}
				} else {
					if (villagerInfo.isFinalTrade(treasure, stack)) {
						PlayerUtils.sendTranslatedChat(player, "chat." + getUnlocalizedName(stack).substring(5) + ".wait");
					} else {
						PlayerUtils.sendTranslatedChat(player, "chat.zss.treasure.uninterested." + treasure.uninterested);
					}
				}
			} else if (entity instanceof INpc) {
				if (treasure == Treasures.KNIGHTS_CREST && entity instanceof EntityNpcOrca) {
					PlayerUtils.sendTranslatedChat(player, "chat.zss.treasure.uninterested." + treasure.uninterested + ".orca");
				} else {
					PlayerUtils.sendTranslatedChat(player, "chat.zss.treasure.uninterested." + treasure.uninterested);
				}
			}
		}
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIconFromDamage(int par1) {
		return iconArray[par1 % Treasures.values().length];
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return super.getUnlocalizedName() + "." + Treasures.values()[stack.getItemDamage() % Treasures.values().length].name;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item item, CreativeTabs tab, List list) {
		for (int i = 0; i < Treasures.values().length; ++i) {
			list.add(new ItemStack(item, 1, i));
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister register) {
		iconArray = new IIcon[Treasures.values().length];
		for (int i = 0; i < Treasures.values().length; ++i) {
			iconArray[i] = register.registerIcon(ModInfo.ID + ":" + Treasures.values()[i].name);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack,	EntityPlayer player, List list, boolean par4) {
		list.add(EnumChatFormatting.ITALIC + StatCollector.translateToLocal("tooltip." + getUnlocalizedName(stack).substring(5) + ".desc.0"));
	}
}
