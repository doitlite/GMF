package com.goldmf.GMFund.extension;

import com.goldmf.GMFund.controller.chat.ChatViewModels;
import com.goldmf.GMFund.manager.stock.SimulationStockManager;

/**
 * Created by yale on 16/4/22.
 */
public class SimulationAccountExtension {
    private SimulationAccountExtension() {
    }

    public static boolean isOpenedSimulationAccount() {
        return SimulationStockManager.getInstance().accountStatus == SimulationStockManager.ACCOUNT_STATE_NORMAL;
    }

    public static boolean isNeedToCheckSimulationAccountState() {
        return SimulationStockManager.getInstance().accountStatus == SimulationStockManager.ACCOUNT_STATE_UNKNOW;
    }
}
