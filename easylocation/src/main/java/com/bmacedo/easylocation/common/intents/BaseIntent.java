package com.bmacedo.easylocation.common.intents;

import android.content.Context;
import android.content.Intent;

/**
 * Created by -Bernardo on 2015-07-22.
 */
public abstract class BaseIntent extends Intent {

    protected abstract String[] getPossibleActions();

    public BaseIntent(Context context, Class<?> clazz, String action) {
        super(context, clazz);
        for (String possibleAction : getPossibleActions()) {
            if (action.equals(possibleAction)) {
                setAction(action);
                return;
            }
        }
        throw new UnsupportedOperationException("Undefined operation for LocationErrorHandlerIntent. ACTION parameter '"+ action +"' not supported.");
    }

    public BaseIntent(final Intent original) {
        super(original);
    }
}
