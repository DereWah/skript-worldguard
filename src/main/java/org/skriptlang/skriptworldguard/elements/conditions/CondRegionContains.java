package org.skriptlang.skriptworldguard.elements.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import org.skriptlang.skriptworldguard.worldguard.WorldGuardRegion;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

@Name("Region Contains")
@Description("A condition that tests whether the given locations are inside of the given regions")
@Examples({
	"player is in the region {spawnregion}",
	"on region enter:",
	"\tworldguard region the region contains {teamflags::red}",
	"\tmessage \"The red flag is near!\""
})
@RequiredPlugins("WorldGuard 7")
@Since("1.0")
public class CondRegionContains extends Condition {

	static { // TODO see what can be done about requiring 'region' as it looks quite dumb in region events with the region expression
		Skript.registerCondition(CondRegionContains.class,
				"[the] [worldguard] region %worldguardregions% contain[s] %locations%",
				"%locations% (is|are) ([contained] in|part of) [the] [worldguard] region %worldguardregions%",
				"[the] [worldguard] region %worldguardregions% (do|does)(n't| not) contain %locations%",
				"%locations% (is|are)(n't| not) (contained in|part of) [the] [worldguard] region %worldguardregions%"
		);
	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<WorldGuardRegion> regions;
	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<Location> locations;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		boolean locationsFirst = matchedPattern % 2 != 0;
		regions = (Expression<WorldGuardRegion>) exprs[locationsFirst ? 1 : 0];
		locations = (Expression<Location>) exprs[locationsFirst ? 0 : 1];
		setNegated(matchedPattern >= 2);
		return true;
	}

	@Override
	public boolean check(Event event) {
		Location[] locations = this.locations.getAll(event);
		return regions.check(event, region -> SimpleExpression.check(
				locations,
				location -> region.getRegion().contains(BukkitAdapter.asBlockVector(location)),
				false,
				this.locations.getAnd()
		), isNegated());
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return regions.toString(event, debug) + " contain" + (regions.isSingle() ? "s" : "") +
				" " + locations.toString(event, debug);
	}

}
