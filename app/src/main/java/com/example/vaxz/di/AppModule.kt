package com.example.vaxz.di

import android.app.Application
import androidx.room.Room
import com.example.vaxz.data.TodoDatabase
import com.example.vaxz.data.TodoRepository
import com.example.vaxz.data.TodoRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideTodoDatabase(app: Application) : TodoDatabase {
        return Room.databaseBuilder(
            app,
            TodoDatabase::class.java,
            "tododb"
        ).build()
    }

    @Provides
    @Singleton
    fun provideTodoRepository(db: TodoDatabase) : TodoRepository {
        return TodoRepositoryImpl(db.dao)
    }
}