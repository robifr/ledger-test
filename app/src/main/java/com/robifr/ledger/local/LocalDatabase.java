/**
 * Copyright 2024 Robi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.robifr.ledger.local;

import android.content.Context;
import android.os.Environment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;
import com.robifr.ledger.data.model.CustomerFtsModel;
import com.robifr.ledger.data.model.CustomerModel;
import com.robifr.ledger.data.model.ProductFtsModel;
import com.robifr.ledger.data.model.ProductModel;
import com.robifr.ledger.data.model.ProductOrderModel;
import com.robifr.ledger.data.model.QueueModel;
import com.robifr.ledger.local.access.CustomerDao;
import com.robifr.ledger.local.access.ProductDao;
import com.robifr.ledger.local.access.ProductOrderDao;
import com.robifr.ledger.local.access.QueueDao;
import java.io.File;
import java.util.Objects;

@Database(
    entities = {
      QueueModel.class,
      CustomerModel.class,
      CustomerFtsModel.class,
      ProductOrderModel.class,
      ProductModel.class,
      ProductFtsModel.class
    },
    version = 1)
public abstract class LocalDatabase extends RoomDatabase {
  @NonNull private static final String _DATA_PATH = LocalDatabase.fileDir() + "/data.db";
  @Nullable private static LocalDatabase _instance;

  @NonNull
  public static synchronized LocalDatabase instance(@NonNull Context context) {
    Objects.requireNonNull(context);

    return LocalDatabase._instance =
        LocalDatabase._instance == null
            ? Room.databaseBuilder(
                    context.getApplicationContext(), LocalDatabase.class, LocalDatabase._DATA_PATH)
                .addCallback(new Callback())
                .fallbackToDestructiveMigration()
                .build()
            : LocalDatabase._instance;
  }

  @Nullable
  public static String fileDir() {
    final String directory =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                .getAbsolutePath()
            + "/.ledger";
    final File dir = new File(directory);

    if (!dir.exists() && !dir.mkdirs()) return null;
    return directory;
  }

  /**
   * @noinspection NullableProblems
   */
  @NonNull
  public abstract QueueDao queueDao();

  /**
   * @noinspection NullableProblems
   */
  @NonNull
  public abstract CustomerDao customerDao();

  /**
   * @noinspection NullableProblems
   */
  @NonNull
  public abstract ProductOrderDao productOrderDao();

  /**
   * @noinspection NullableProblems
   */
  @NonNull
  public abstract ProductDao productDao();

  public static class Callback extends RoomDatabase.Callback {
    @Override
    public void onCreate(@NonNull SupportSQLiteDatabase db) {
      // Rebuild FTS index.
      db.execSQL("INSERT INTO customer_fts(customer_fts) VALUES ('rebuild')");
      super.onCreate(db);
    }
  }
}
