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

package com.robifr.ledger.ui.editproduct.viewmodel

import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateHandle
import com.robifr.ledger.InstantTaskExecutorExtension
import com.robifr.ledger.LifecycleOwnerExtension
import com.robifr.ledger.LifecycleTestOwner
import com.robifr.ledger.MainCoroutineExtension
import com.robifr.ledger.awaitValue
import com.robifr.ledger.data.model.ProductModel
import com.robifr.ledger.repository.ProductRepository
import com.robifr.ledger.ui.SafeEvent
import com.robifr.ledger.ui.createproduct.viewmodel.CreateProductState
import com.robifr.ledger.ui.editproduct.EditProductFragment
import com.robifr.ledger.util.CurrencyFormat
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.concurrent.CompletableFuture
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

@ExperimentalCoroutinesApi
@ExtendWith(
    InstantTaskExecutorExtension::class,
    MainCoroutineExtension::class,
    LifecycleOwnerExtension::class)
class EditProductViewModelTest(
    private val _dispatcher: TestDispatcher,
    private val _lifecycleOwner: LifecycleTestOwner
) {
  private lateinit var _resultStateObserver: Observer<SafeEvent<EditProductResultState>>
  private lateinit var _productRepository: ProductRepository
  private lateinit var _viewModel: EditProductViewModel

  private val _productToEdit: ProductModel = ProductModel(id = 111L, name = "Apple", price = 100)

  @BeforeEach
  fun beforeEach() {
    clearAllMocks()
    _resultStateObserver = mockk(relaxed = true)
    _productRepository = mockk()

    every { _productRepository.add(any()) } returns CompletableFuture.completedFuture(0L)
    every { _productRepository.selectById(_productToEdit.id) } returns
        CompletableFuture.completedFuture(_productToEdit)
    _viewModel =
        EditProductViewModel(
            _productRepository,
            _dispatcher,
            SavedStateHandle().apply {
              set(
                  EditProductFragment.Arguments.INITIAL_PRODUCT_ID_TO_EDIT_LONG.key,
                  _productToEdit.id)
            })
    _viewModel.editResultState.observe(_lifecycleOwner, _resultStateObserver)
  }

  @Test
  fun `on initialize with arguments`() {
    assertEquals(
        CreateProductState(
            name = _productToEdit.name,
            nameErrorMessageRes = null,
            formattedPrice = CurrencyFormat.format(_productToEdit.price.toBigDecimal(), "")),
        _viewModel.uiState.safeValue,
        "The state should match the retrieved data based from the provided product ID")
  }

  @Test
  fun `on save with blank name`() {
    _viewModel.onNameTextChanged(" ")

    every { _productRepository.update(any()) } returns CompletableFuture.completedFuture(0)
    _viewModel.onSave()
    assertNotNull(_viewModel.uiState.safeValue.nameErrorMessageRes, "Show error for a blank name")
    assertDoesNotThrow("Prevent save for a blank name") {
      verify(exactly = 0) { _productRepository.update(any()) }
    }
  }

  @Test
  fun `on save with edited product result update operation`() {
    // Prevent save with add operation (parent class behavior) instead of update operation.
    every { _productRepository.update(any()) } returns CompletableFuture.completedFuture(0)
    _viewModel.onSave()
    assertDoesNotThrow("Editing a product shouldn't result in adding new data") {
      verify(exactly = 0) { _productRepository.add(any()) }
      verify(exactly = 1) { _productRepository.update(any()) }
    }
  }

  @ParameterizedTest
  @ValueSource(ints = [0, 1])
  fun `on save with edited product`(effectedRows: Int) {
    every { _productRepository.update(any()) } returns
        CompletableFuture.completedFuture(effectedRows)
    _viewModel.onSave()
    if (effectedRows == 0) {
      assertDoesNotThrow("Don't return result for a failed save") {
        verify(exactly = 0) { _resultStateObserver.onChanged(any()) }
      }
    } else {
      assertEquals(
          _productToEdit.id,
          _viewModel.editResultState.awaitValue().valueIfNotHandled?.editedProductId,
          "Return result with the correct ID after success save")
    }
  }
}