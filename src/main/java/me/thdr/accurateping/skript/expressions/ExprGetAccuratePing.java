package me.thdr.accurateping.skript.expressions;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import me.thdr.accurateping.AccuratePing;
import me.thdr.accurateping.PingListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;
import org.bukkit.plugin.Plugin;

public class ExprGetAccuratePing extends SimpleExpression<Long> {

    private Expression<Player> player;

    static {
        SimplePropertyExpression.register(ExprGetAccuratePing.class, Long.class, "accurate ping", "player");
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        player = (Expression<Player>) exprs[0];
        return true;
    }

    @Override
    protected Long @NotNull [] get(@NotNull Event e) {
        Player p = player.getSingle(e);
        if (p == null) {
            return null;
        }
        // get the plugin instance
        AccuratePing plugin = (AccuratePing) Bukkit.getPluginManager().getPlugin("AccuratePing");
        if (plugin == null) {
            System.out.println("AccuratePing plugin instance is null!!! This should not happen!");
            return null;
        }
        // get the ping listener instance
        PingListener pingListener = plugin.getPingListener();
        if (pingListener == null) {
            System.out.println("PingListener instance is null!!! This should not happen!");
            return null;
        }
        return new Long[]{pingListener.getPing(p)};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public @NotNull Class<? extends Long> getReturnType() {
        return Long.class;
    }

    @Override
    public String toString(@Nullable Event e, boolean debug) {
        return "get accurate ping of " + player.toString(e, debug);
    }
}