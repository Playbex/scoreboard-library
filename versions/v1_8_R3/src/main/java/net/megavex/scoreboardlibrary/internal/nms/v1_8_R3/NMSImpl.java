package net.megavex.scoreboardlibrary.internal.nms.v1_8_R3;

import java.lang.reflect.Field;
import net.megavex.scoreboardlibrary.api.sidebar.Sidebar;
import net.megavex.scoreboardlibrary.internal.nms.base.ScoreboardManagerNMS;
import net.megavex.scoreboardlibrary.internal.nms.base.SidebarNMS;
import net.megavex.scoreboardlibrary.internal.nms.base.TeamNMS;
import net.megavex.scoreboardlibrary.internal.nms.base.util.UnsafeUtilities;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayOutScoreboardDisplayObjective;
import net.minecraft.server.v1_8_R3.PacketPlayOutScoreboardObjective;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class NMSImpl extends ScoreboardManagerNMS<Packet<?>> {
  static final Field objectiveModeField;

  static {
    objectiveModeField = UnsafeUtilities.getField(PacketPlayOutScoreboardObjective.class, "d");
  }

  private final PacketPlayOutScoreboardDisplayObjective displayPacket = new PacketPlayOutScoreboardDisplayObjective();
  private final PacketPlayOutScoreboardObjective removePacket = new PacketPlayOutScoreboardObjective();

  public NMSImpl() {
    // Setup static packets
    UnsafeUtilities.UNSAFE.putInt(
      displayPacket,
      UnsafeUtilities.UNSAFE.objectFieldOffset(UnsafeUtilities.getField(PacketPlayOutScoreboardDisplayObjective.class, "a")),
      1
    );
    UnsafeUtilities.setField(UnsafeUtilities.getField(PacketPlayOutScoreboardDisplayObjective.class, "b"), displayPacket, objectiveName);

    UnsafeUtilities.setField(UnsafeUtilities.getField(PacketPlayOutScoreboardObjective.class, "a"), removePacket, objectiveName);
    UnsafeUtilities.UNSAFE.putInt(
      removePacket,
      UnsafeUtilities.UNSAFE.objectFieldOffset(objectiveModeField),
      1
    );
  }

  @Override
  public @NotNull SidebarNMS<Packet<?>, ?> createSidebarNMS(@NotNull Sidebar sidebar) {
    return new SidebarNMSImpl(this, sidebar);
  }

  @Override
  public void displaySidebar(@NotNull Iterable<Player> players) {
    sendPacket(players, displayPacket);
  }

  @Override
  public void removeSidebar(@NotNull Iterable<Player> players) {
    sendPacket(players, removePacket);
  }

  @Override
  public @NotNull TeamNMS<?, ?> createTeamNMS(@NotNull String teamName) {
    return new TeamNMSImpl(this, teamName);
  }

  @Override
  public boolean isLegacy(@NotNull Player player) {
    return true;
  }

  @Override
  public void sendPacket(@NotNull Player player, @NotNull Packet<?> packet) {
    ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
  }
}
