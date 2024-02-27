/**
 * Copyright (c) 2022-present Robi
 *
 * Ledger is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Ledger is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Ledger. If not, see <https://www.gnu.org/licenses/>.
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
