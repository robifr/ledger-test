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

package com.robifr.ledger.data.model

sealed interface Model {
  /**
   * Model primary key.
   *
   * Note: The type is nullable instead of its primitive type even though both of them are valid.
   * It's due to when updating a model with foreign-key set to zero — indicating the referenced row
   * was deleted — instead of null, the query will silently fail.
   */
  fun modelId(): Long?
}
