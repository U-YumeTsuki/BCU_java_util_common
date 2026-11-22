package common.util.stage.info;

import common.util.stage.Stage;

import javax.annotation.Nullable;

public interface StageInfo {

    byte FEVER_COST_START = 0, FEVER_COST_END = 1, FEVER_MULTIPLIER = 2;

    Stage getStage();

    boolean exConnection();

    Stage[] getExStages();

    float[] getExChances();

    @Nullable int[][] getFever();
}