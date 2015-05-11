package pt.uminho.ceb.biosystems.mew.mewcore.optimization.components;

/**
 * Created by ptiago on 12-01-2015.
 */
public class ReactionSwapData {
    protected String originalReaction;
    protected String swapReaction;

    public ReactionSwapData(String originalReaction, String swapReaction) {
        this.originalReaction = originalReaction;
        this.swapReaction = swapReaction;
    }

    public String getSwapReaction() {
        return swapReaction;
    }

    public String getOriginalReaction() {
        return originalReaction;
    }
}
