/**
 * Copyright (c) 2024 Robi
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

package com.robifr.ledger.assetbinding.chart;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Objects;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public interface ChartData {
  @NonNull
  public default JSONObject toJson() {
    final JSONObject json = new JSONObject();
    final Field[] fields = this.getClass().getDeclaredFields();

    for (Field field : fields) {
      // Although fields in record class are public,
      // it's still required to provide private field to be accessible.
      field.setAccessible(true);

      try {
        final Object fieldValue = field.get(this);

        if (fieldValue == null) {
          json.put(field.getName(), JSONObject.NULL);

        } else if (fieldValue.getClass().isArray()) {
          json.put(field.getName(), new JSONArray(fieldValue));

        } else if (fieldValue instanceof Collection collection) {
          json.put(field.getName(), new JSONArray(collection));

        } else {
          json.put(field.getName(), fieldValue);
        }

      } catch (IllegalAccessException | JSONException e) {
        throw new RuntimeException(e);
      }
    }

    return json;
  }

  public static record Single<K, V>(@NonNull @Keep K key, @NonNull @Keep V value)
      implements ChartData {
    public Single {
      Objects.requireNonNull(key);
      Objects.requireNonNull(value);
    }
  }

  public static record Multiple<K, V, G>(
      @NonNull @Keep K key, @NonNull @Keep V value, @NonNull @Keep G group) implements ChartData {
    public Multiple {
      Objects.requireNonNull(key);
      Objects.requireNonNull(value);
      Objects.requireNonNull(group);
    }
  }
}
