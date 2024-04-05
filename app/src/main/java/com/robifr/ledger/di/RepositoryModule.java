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

package com.robifr.ledger.di;

import android.content.Context;
import androidx.annotation.NonNull;
import com.robifr.ledger.repository.CustomerRepository;
import com.robifr.ledger.repository.ProductOrderRepository;
import com.robifr.ledger.repository.ProductRepository;
import com.robifr.ledger.repository.QueueRepository;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ViewModelComponent;
import dagger.hilt.android.qualifiers.ApplicationContext;
import java.util.Objects;

@Module
@InstallIn(ViewModelComponent.class)
public class RepositoryModule {
  @NonNull
  @Provides
  public CustomerRepository provideCustomerRepository(
      @NonNull @ApplicationContext Context context) {
    Objects.requireNonNull(context);

    return CustomerRepository.instance(context);
  }

  @NonNull
  @Provides
  public ProductRepository provideProductRepository(@NonNull @ApplicationContext Context context) {
    Objects.requireNonNull(context);

    return ProductRepository.instance(context);
  }

  @NonNull
  @Provides
  public ProductOrderRepository provideProductOrderRepository(
      @NonNull @ApplicationContext Context context) {
    Objects.requireNonNull(context);

    return ProductOrderRepository.instance(context);
  }

  @NonNull
  @Provides
  public QueueRepository provideQueueRepository(@NonNull @ApplicationContext Context context) {
    Objects.requireNonNull(context);

    return QueueRepository.instance(context);
  }
}
