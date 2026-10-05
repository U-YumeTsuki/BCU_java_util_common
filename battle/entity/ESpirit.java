package common.battle.entity;

import common.battle.StageBasis;
import common.battle.attack.AttackAb;
import common.battle.data.MaskAtk;
import common.battle.data.MaskUnit;
import common.util.anim.EAnimU;
import common.util.unit.Level;

public class ESpirit extends EUnit {

    public ESpirit(StageBasis b, MaskUnit de, EAnimU ea, float lvd, int minlayer, int maxlayer, Level lv, int[] ind) {
        super(b, de, ea, lvd, minlayer, maxlayer, lv, null, ind, false);
        atkm.startAtk(false);
    }

    @Override
    public float calcDamageMult(int dmg, Entity e, MaskAtk matk) {
        return 0;
    }

    @Override
    public boolean damaged(AttackAb atk) {
        if (status.inv[0] != -1)
            return false;
        status.inv[0] = Integer.MAX_VALUE;
        anim.getEff(P_IMUATK);
        return false;
    }

    @Override
    public int getAbi() {
        return data.getAbi() | AB_IMUSW;
    }

    @Override
    public void preUpdate() {
    }

    @Override
    public void update() {
        auras.updateAuras();
        // update attack status when in attack state
        if (anim.dead > 0)
            anim.dead -= getTimeFreeze();
        else if (atkm.atkTime > 1)
            atkm.updateAttack();
        else
            atkm.atkTime -= getTimeFreeze();
        updateAnimation();
    }

    @Override
    public void postUpdate() {
        if ((getAbi() & AB_GLASS) > 0 && atkm.atkTime <= 0 && atkm.attacksLeft == 0)
            kill(true);

        if(!dead || !summoned.isEmpty())
            livingTime++;
        summoned.removeIf(s -> !s.activate);
    }

    @Override
    public int touchable() {
        return kbTime == -1 ? TCH_SOUL : TCH_N;
    }
}
