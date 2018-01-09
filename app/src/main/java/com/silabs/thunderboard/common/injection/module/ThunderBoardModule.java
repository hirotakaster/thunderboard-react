package com.silabs.thunderboard.common.injection.module;

import android.content.Context;

import com.silabs.thunderboard.common.injection.qualifier.ForApplication;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class ThunderBoardModule {

    private final Context context;

    public ThunderBoardModule(Context context) {
        this.context = context;
    }

    @Provides
    @Singleton
    @ForApplication
    Context provideContext() {
        return context;
    }

}
