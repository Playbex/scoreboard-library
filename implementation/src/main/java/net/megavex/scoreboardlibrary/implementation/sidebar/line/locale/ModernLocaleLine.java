package net.megavex.scoreboardlibrary.implementation.sidebar.line.locale;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.megavex.scoreboardlibrary.implementation.packetAdapter.ImmutableTeamProperties;
import net.megavex.scoreboardlibrary.implementation.packetAdapter.PropertiesPacketType;
import net.megavex.scoreboardlibrary.implementation.packetAdapter.team.TeamDisplayPacketAdapter;
import net.megavex.scoreboardlibrary.implementation.sidebar.line.GlobalLineInfo;
import net.megavex.scoreboardlibrary.implementation.sidebar.line.SidebarLineHandler;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;

import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection;

// Implementation for versions above 1.13
public class ModernLocaleLine implements LocaleLine, ImmutableTeamProperties<Component> {
  private final GlobalLineInfo info;
  private final SidebarLineHandler handler;
  private final Collection<String> entries;
  private final TeamDisplayPacketAdapter packetAdapter;
  private String player, oldPlayer;
  private String prefix, suffix;
  private String currentValue;


  public ModernLocaleLine(GlobalLineInfo info, SidebarLineHandler handler) {
    this.info = info;
    this.handler = handler;
    this.player = info.player();
    this.entries = Collections.singleton(info.player());
    this.packetAdapter = info.packetAdapter().createTeamDisplayAdapter(this);
    packetAdapter.updateTeamPackets(entries);
  }

  @Override
  public @NotNull GlobalLineInfo info() {
    return info;
  }

  @Override
  public void value(@NotNull Component renderedComponent) {
    String legacyValue = legacySection().serialize(renderedComponent);
    String prevPlayer = player;

    if (legacyValue.length() <= 16) {
      this.prefix = legacyValue;
      this.suffix = "";
      if (this.currentValue != null && this.currentValue.length() > 32) {
        this.player = info.player();
      }
    } else {
      handleLongLegacyValue(legacyValue);
    }

    currentValue = legacyValue;
    if (!player.equals(prevPlayer)) {
      oldPlayer = prevPlayer;
    }
  }

  private void handleLongLegacyValue(String legacyValue) {
    boolean endsWithSection = legacyValue.charAt(15) == LegacyComponentSerializer.SECTION_CHAR;
    int prefixEnd = endsWithSection ? 15 : 16;
    this.prefix = legacyValue.substring(0, prefixEnd);

    String last = prefix + LegacyComponentSerializer.SECTION_CHAR + (endsWithSection ? legacyValue.charAt(16) : "");
    this.player = info.player() + ChatColor.RESET + ChatColor.getLastColors(last);

    int playerEnd = prefixEnd;
    if (legacyValue.length() > 32) {
      int remaining = 16 - player.length();
      assert remaining > 0;

      playerEnd += remaining;
      player += legacyValue.substring(prefixEnd, playerEnd);
    }

    this.suffix = legacyValue.substring(playerEnd + (endsWithSection ? 2 : 0));
    if (suffix.length() > 16) {
      suffix = adjustSuffixLength(suffix);
    }
  }

  private String adjustSuffixLength(String suffix) {
    String newSuffix = suffix.substring(0, 16);
    if (newSuffix.endsWith(String.valueOf(LegacyComponentSerializer.SECTION_CHAR)) &&
      ChatColor.getByChar(suffix.charAt(16)) != null) {
      newSuffix = newSuffix.substring(0, 15);
    }
    return newSuffix;
  }

  @Override
  public void updateTeam() {
    packetAdapter.updateTeamPackets(entries);
    packetAdapter.sendProperties(PropertiesPacketType.UPDATE, handler.players());
  }

  @Override
  public void sendScore(@NotNull Collection<Player> players) {
    handler.localeLineHandler()
      .sidebar()
      .packetAdapter()
      .sendScore(players, info.player(), info.objectiveScore(), null, info.scoreFormat());
  }

  @Override
  public void show(@NotNull Collection<Player> players) {
    sendScore(players);
    packetAdapter.sendProperties(PropertiesPacketType.CREATE, players);
  }

  @Override
  public void hide(@NotNull Collection<Player> players) {
    handler.localeLineHandler().sidebar().packetAdapter().removeScore(players, info.player());
    info.packetAdapter().removeTeam(players);
  }

  @Override
  public @NotNull Collection<String> entries() {
    return entries;
  }

  @Override
  public @NotNull Component displayName() {
    return empty();
  }

  @Override
  public @NotNull Component prefix() {
    return Component.text(prefix);
  }

  @Override
  public @NotNull Component suffix() {
    return Component.text(suffix);
  }
}
