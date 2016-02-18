
package com.techjar.ledcm.hardware.animation.sequence;

/**
 * Dummy class, only used in loading code.
 *
 * @author Techjar
 */
public class SequenceCommandMusicSync extends SequenceCommand {
    public SequenceCommandMusicSync(AnimationSequence sequence) {
        super(sequence);
    }

    @Override
    public boolean execute(String[] args) {
        return true;
    }

    @Override
    public boolean onSequenceLoad(String[] args) {
        sequence.setMusicSynced(true);
        return false;
    }
}
