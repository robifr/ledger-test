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
