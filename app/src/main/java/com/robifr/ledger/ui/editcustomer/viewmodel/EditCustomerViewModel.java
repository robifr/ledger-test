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

package com.robifr.ledger.ui.editcustomer.viewmodel;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.robifr.ledger.R;
import com.robifr.ledger.data.model.CustomerModel;
import com.robifr.ledger.repository.CustomerRepository;
import com.robifr.ledger.ui.LiveDataEvent;
import com.robifr.ledger.ui.StringResources;
import com.robifr.ledger.ui.createcustomer.viewmodel.CreateCustomerViewModel;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class EditCustomerViewModel extends CreateCustomerViewModel {
  @NonNull
  private final MutableLiveData<LiveDataEvent<Long>> _editedCustomerId = new MutableLiveData<>();

  @Nullable private CustomerModel _initialCustomerToEdit = null;

  public EditCustomerViewModel(@NonNull CustomerRepository customerRepository) {
    super(customerRepository);
  }

  @Override
  @NonNull
  public CustomerModel inputtedCustomer() {
    final Long id =
        this._initialCustomerToEdit != null && this._initialCustomerToEdit.id() != null
            ? this._initialCustomerToEdit.id()
            : null;
    return CustomerModel.toBuilder(super.inputtedCustomer()).setId(id).build();
  }

  @Override
  public void onSave() {
    if (this.inputtedCustomer().name().isBlank()) {
      this._inputtedNameError.setValue(
          new LiveDataEvent<>(
              new StringResources.Strings(R.string.text_customer_name_is_required)));
      return;
    }

    this._updateCustomer(this.inputtedCustomer());
  }

  public void setInitialCustomerToEdit(@NonNull CustomerModel customer) {
    this._initialCustomerToEdit = Objects.requireNonNull(customer);
  }

  @NonNull
  public LiveData<LiveDataEvent<Long>> editedCustomerId() {
    return this._editedCustomerId;
  }

  @Nullable
  public CustomerModel selectCustomerById(@Nullable Long customerId) {
    final StringResources notFoundRes =
        new StringResources.Strings(R.string.text_error_failed_to_find_related_customer);
    CustomerModel customer = null;

    try {
      customer = this._customerRepository.selectById(customerId).get();
      if (customer == null) this._snackbarMessage.setValue(new LiveDataEvent<>(notFoundRes));

    } catch (ExecutionException | InterruptedException e) {
      this._snackbarMessage.setValue(new LiveDataEvent<>(notFoundRes));
    }

    return customer;
  }

  private void _updateCustomer(@NonNull CustomerModel customer) {
    Objects.requireNonNull(customer);

    this._customerRepository
        .update(customer)
        .thenAcceptAsync(
            effected -> {
              if (effected > 0) {
                this._editedCustomerId.postValue(new LiveDataEvent<>(customer.id()));
              }

              final StringResources stringRes =
                  effected > 0
                      ? new StringResources.Plurals(
                          R.plurals.args_updated_x_customer, effected, effected)
                      : new StringResources.Strings(R.string.text_error_failed_to_update_customer);
              this._snackbarMessage.postValue(new LiveDataEvent<>(stringRes));
            });
  }

  public static class Factory implements ViewModelProvider.Factory {
    @NonNull private final Context _context;

    public Factory(@NonNull Context context) {
      Objects.requireNonNull(context);

      this._context = context.getApplicationContext();
    }

    @Override
    @NonNull
    public <T extends ViewModel> T create(@NonNull Class<T> cls) {
      Objects.requireNonNull(cls);

      final EditCustomerViewModel viewModel =
          new EditCustomerViewModel(CustomerRepository.instance(this._context));
      return Objects.requireNonNull(cls.cast(viewModel));
    }
  }
}
