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

package zeldaswordskills.songs;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.WorldInfo;
import zeldaswordskills.network.PacketDispatcher;
import zeldaswordskills.network.bidirectional.PlaySoundPacket;
import zeldaswordskills.ref.Config;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.util.PlayerUtils;
import zeldaswordskills.util.SongNote;


/**
 * Play to change the weather.
 * Time between uses may be restricted via Config settings.
 */
public class ZeldaSongStorms extends AbstractZeldaSong
{
	/** Next world time value at which the song may be used */
	private static long nextChange;

	public ZeldaSongStorms(String unlocalizedName, int minDuration, SongNote... notes) {
		super(unlocalizedName, minDuration, notes);
	}

	@Override
	protected void performEffect(EntityPlayer player, ItemStack instrument, int power) {
		if (power > 4 && player.worldObj instanceof WorldServer) {
			if (!player.capabilities.isCreativeMode && player.worldObj.getWorldTime() < nextChange) {
				PlayerUtils.sendFormattedChat(player, "chat.zss.song.cooldown", getDisplayName(), Config.getMinIntervalStorm());
				return;
			}
			nextChange = player.worldObj.getWorldTime() + Config.getMinIntervalStorm();
			PacketDispatcher.sendTo(new PlaySoundPacket(Sounds.SUCCESS, 1.0F, 1.0F), (EntityPlayerMP) player);
			WorldInfo worldinfo = ((WorldServer) player.worldObj).getWorldInfo();
			if (worldinfo.isRaining()) {
				worldinfo.setRainTime(0);
				worldinfo.setRaining(false);
			} else {
				worldinfo.setRainTime(2000);
				worldinfo.setRaining(true);
			}
			if (worldinfo.isThundering()) {
				worldinfo.setThunderTime(0);
				worldinfo.setThundering(false);
			} else {
				worldinfo.setThunderTime(2000);
				worldinfo.setThundering(true);
			}
		}
	}
}
