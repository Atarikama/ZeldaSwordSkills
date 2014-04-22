/**
    Copyright (C) <2014> <coolAlias>

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

package zeldaswordskills.skills.sword;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import zeldaswordskills.entity.ZSSPlayerInfo;
import zeldaswordskills.skills.SkillActive;
import zeldaswordskills.util.PlayerUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 
 * RISING CUT
 * Description: Rising slash flings enemy upward
 * Activation: Jump while sneaking and attack
 * Effect: Attacks target for normal sword damage and knocks the target into the air
 * Range: 2 + level blocks
 * Exhaustion: 3.0F - (level * 0.2F)
 * Special: May only be used while locked on to a target
 * 
 * Requires onRenderTick to be called each render tick while active.
 *  
 */
public class RisingCut extends SkillActive
{
	/** Flag for activation; set when player jumps while sneaking */
	@SideOnly(Side.CLIENT)
	private int ticksTilFail;
	/** True while animation is in progress */
	private int activeTimer;
	/** Stores the entity struck to add velocity on the next update */
	private Entity entityHit;

	public RisingCut(String name) {
		super(name);
		setDisablesLMB();
	}

	private RisingCut(RisingCut skill) {
		super(skill);
	}

	@Override
	public RisingCut newInstance() {
		return new RisingCut(this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(List<String> desc, EntityPlayer player) {
		desc.add(getRangeDisplay(2 + level));
		desc.add(getExhaustionDisplay(getExhaustion()));
	}

	@Override
	public boolean isActive() {
		return activeTimer > 0;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean canExecute(EntityPlayer player) {
		return ticksTilFail > 0 && player.motionY > 0.0D && canUse(player);
	}

	@Override
	public boolean canUse(EntityPlayer player) {
		return super.canUse(player) && !isActive() && PlayerUtils.isHoldingSword(player);
	}

	@Override
	protected float getExhaustion() {
		return 3.0F - (level * 0.2F);
	}

	@Override
	public boolean activate(World world, EntityPlayer player) {
		if (super.activate(world, player)) {
			activeTimer = 5 + level;
			player.motionY += 0.3D + (0.115D * level);
			ZSSPlayerInfo.get(player).reduceFallAmount = level;
		}
		return isActive();
	}

	@Override
	public void onUpdate(EntityPlayer player) {
		if (player.worldObj.isRemote && ticksTilFail > 0) {
			--ticksTilFail;
		}
		if (isActive()) {
			--activeTimer;
			if (entityHit != null) {
				if (!entityHit.isDead) {
					double addY = 0.3D + (0.125D * level);
					double resist = 1.0D;
					if (entityHit instanceof EntityLivingBase) {
						resist = 1.0D - ((EntityLivingBase) entityHit).getEntityAttribute(SharedMonsterAttributes.knockbackResistance).getAttributeValue();
					}
					entityHit.addVelocity(0.0D, addY * resist, 0.0D);
				}
				entityHit = null;
			}
		}
	}

	/**
	 * Flags the skill as ready to be activated when the player next attacks,
	 * provided canExecute(EntityPlayer) returns true at that time
	 */
	@SideOnly(Side.CLIENT)
	public void keyPressed() {
		ticksTilFail = 3;
	}

	/**
	 * Keeps player's sword fully extended for duration of skill
	 * @return always false so that player remains looking at the target
	 */
	@SideOnly(Side.CLIENT)
	public boolean onRenderTick(EntityPlayer player) {
		player.swingProgress = 0.5F;
		return false;
	}

	/**
	 * Call when an entity is damaged to flag the entity for velocity update next tick.
	 * This is necessary because adding velocity right before the entity is damaged fails.
	 */
	public void onImpact(Entity entity) {
		this.entityHit = entity;
	}
}
