// ==========================================
// ASM ORDER MANAGEMENT JAVASCRIPT
// ==========================================

let distributorList = [];
let distributorStockList = [];
let employeeHeadquarters = "";
let stockList = [];
let doctorTomSelect = null;
let productTomSelect = null;
let productRowIndex = 0;
let productChoices = null;
let historyList = [];
let editingOrderId = null;
let editOrderModal;
let editingOrderGroupId = null;
let editingOrders = [];
let allProducts = [];
let allDoctors = [];

// Helper function to safely get ID whether it's stored as asmId or employeeId
function getAsmId() {
  return localStorage.getItem("asmId") || localStorage.getItem("employeeId");
}

function getAsmName() {
  return localStorage.getItem("asmName") || localStorage.getItem("employeeName");
}

window.onload = function () {
  if (typeof checkAsmSession === "function") {
    checkAsmSession();
  }
  
  loadAssignedDoctors();
  loadEmployeeAndDistributors();
  loadOrderHistory();
  loadAllProducts();
  loadAllDoctors();

  const orderDateInput = document.getElementById("orderDate");
  if (orderDateInput) {
    orderDateInput.value = new Date().toISOString().split("T")[0];
  }

  setTimeout(() => {
    addCreateProductRow();
  }, 500);

  editOrderModal = new bootstrap.Modal(document.getElementById("editOrderModal"));
};

function loadAssignedDoctors() {
  const empId = getAsmId();
  fetch(`${BASE_URL}/doctor-visit/unique-doctors/${empId}`)
    .then((response) => response.json())
    .then((data) => {
      const dropdown = document.getElementById("doctorId");
      if (doctorTomSelect) {
        doctorTomSelect.destroy();
        doctorTomSelect = null;
      }
      dropdown.innerHTML = `<option value="">Select Doctor </option>`;
      data.forEach((doctor) => {
        dropdown.innerHTML += `
          <option value="${doctor.id}"
            data-name="${doctor.doctorName}"
            data-category="${doctor.visitCategory || "DOCTOR"}"
            data-landmark="${doctor.landmark || ""}">
            ${doctor.doctorName} - ${doctor.visitCategory === "CHEMIST" ? "Chemist" : "Doctor"} - ${doctor.hospitalName || ""}
          </option>
        `;
      });
      doctorTomSelect = new TomSelect("#doctorId", {
        create: false,
        placeholder: "Search Doctor...",
      });
    })
    .catch((error) => console.error(error));
}

function loadEmployeeAndDistributors() {
  const empId = getAsmId();
  fetch(`${BASE_URL}/employee/${empId}`)
    .then((response) => response.json())
    .then((employee) => {
      employeeHeadquarters = employee.headquarters || employee.headQuarters || employee.headQuarter || "";
      document.getElementById("headquarters").value = employeeHeadquarters;
      return fetch(`${BASE_URL}/distributor/by-headquarter/${encodeURIComponent(employeeHeadquarters)}`);
    })
    .then((response) => response.json())
    .then((data) => {
      distributorList = data || [];
      const dropdown = document.getElementById("distributorId");
      dropdown.innerHTML = `<option value="">Select Distributor</option>`;
      distributorList.forEach((d) => {
        dropdown.innerHTML += `<option value="${d.id}" data-name="${d.distributorName}">${d.distributorName}</option>`;
      });
    });
}

function loadDistributorProducts() {
  const distributorId = document.getElementById("distributorId").value;
  const selected = document.getElementById("distributorId").selectedOptions[0];
  document.getElementById("distributorName").value = selected ? selected.getAttribute("data-name") : "";
  
  if (!distributorId) {
    distributorStockList = [];
    document.getElementById("productItems").innerHTML = "";
    return;
  }
  
  fetch(`${BASE_URL}/distributor-stock/${distributorId}`)
    .then((response) => response.json())
    .then((data) => {
      distributorStockList = data || [];
      stockList = distributorStockList;
      document.getElementById("productItems").innerHTML = "";
      productRowIndex = 0;
      addCreateProductRow(); // Automatically add first row
    });
}

function getProductOptions() {
  let options = "";
  stockList.forEach((product) => {
    options += `<option value="${product.productId}">${product.productName} - (${product.availableUnits} units)</option>`;
  });
  return options;
}

function addCreateProductRow() {
  const container = document.getElementById("productItems");
  const rowId = productRowIndex++;
  container.insertAdjacentHTML(
    "beforeend",
    `
    <div class="row g-3 align-items-end product-row mb-3 border rounded-4 p-3" id="productRow${rowId}">
      <div class="col-md-4">
        <label class="form-label small text-muted">Select Product</label>
        <select class="form-select productSelect" id="productSelect${rowId}" onchange="setRowProductDetails(this)">
          ${getProductOptions()}
        </select>
      </div>
      <div class="col-md-2 col-4">
        <label class="form-label small text-muted">Available</label>
        <input type="text" class="form-control availableStock bg-light" readonly value="0 Units">
      </div>
      <div class="col-md-2 col-4">
        <label class="form-label small text-muted">Quantity</label>
        <input type="number" class="form-control orderQuantity" oninput="calculateRowAmount(this)">
      </div>
      <div class="col-md-2 col-4">
        <label class="form-label small text-muted">Price</label>
        <input type="number" class="form-control sellingPrice" oninput="calculateRowAmount(this)">
      </div>
      <div class="col-md-2">
        <label class="form-label small text-muted">Amount</label>
        <input type="number" class="form-control rowAmount bg-light" readonly>
      </div>
      <div class="col-md-12 text-end">
        <button type="button" class="btn btn-sm btn-outline-danger" onclick="removeCreateProductRow(${rowId})">
          <i class="fa-solid fa-trash me-1"></i> Remove
        </button>
      </div>
    </div>
    `
  );
  
  setTimeout(() => {
    new Choices(`#productSelect${rowId}`, {
      searchEnabled: true,
      itemSelectText: "",
      shouldSort: false,
      placeholder: true,
      placeholderValue: "Select Product",
      searchPlaceholderValue: "Search product...",
    });
  }, 100);
}

function removeCreateProductRow(rowId) {
  document.getElementById("productRow" + rowId).remove();
  calculateTotalOrderAmount();
}

function setRowProductDetails(selectElement) {
  const row = selectElement.closest(".product-row");
  const productId = selectElement.value;
  const selectedProduct = stockList.find((p) => String(p.productId) === String(productId));
  
  if (!selectedProduct) {
    row.querySelector(".availableStock").value = "0 Units";
    calculateTotalOrderAmount();
    return;
  }
  
  row.querySelector(".availableStock").value = selectedProduct.availableUnits + " Units";
  calculateRowAmount(selectElement);
}

function calculateRowAmount(element) {
  const row = element.closest(".product-row");
  const qty = Number(row.querySelector(".orderQuantity").value || 0);
  const price = Number(row.querySelector(".sellingPrice").value || 0);
  row.querySelector(".rowAmount").value = qty * price;
  calculateTotalOrderAmount();
}

function calculateTotalOrderAmount() {
  let total = 0;
  document.querySelectorAll(".rowAmount").forEach((input) => {
    total += Number(input.value || 0);
  });
  document.getElementById("orderAmount").value = total;
  calculateDueAmount();
}

function setDoctorName() {
  const select = document.getElementById("doctorId");
  const selected = select.options[select.selectedIndex];
  document.getElementById("doctorName").value = selected ? selected.getAttribute("data-name") || "" : "";
  document.getElementById("visitCategory").value = selected ? selected.getAttribute("data-category") || "" : "";
  document.getElementById("area").value = selected ? selected.getAttribute("data-landmark") || "" : "";
}

function getLocalDate() {
  const now = new Date();
  const year = now.getFullYear();
  const month = String(now.getMonth() + 1).padStart(2, "0");
  const day = String(now.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
}

function placeOrder() {
  const area = document.getElementById("area").value.trim();
  const doctorId = document.getElementById("doctorId").value;
  const doctorName = document.getElementById("doctorName").value;
  const visitCategory = document.getElementById("visitCategory").value;
  const distributorId = document.getElementById("distributorId").value;
  const distributorName = document.getElementById("distributorName").value;
  const headquarters = document.getElementById("headquarters").value;
  const paymentMode = document.getElementById("paymentMode").value;
  
  if (!doctorId) return alert("Please select doctor");
  if (!visitCategory) return alert("Selected Doctor/Chemist category is missing");
  if (!distributorId) return alert("Please select distributor");
  if (!paymentMode) return alert("Please select payment mode");
  if (!area) return alert("Please enter area");
  
  const rows = document.querySelectorAll(".product-row");
  if (rows.length === 0) return alert("Please add at least one product");
  
  const paidAmountInput = document.getElementById("paidAmount").value.trim();
  if (paidAmountInput === "") return alert("Please enter paid amount (enter 0 if no payment received)");
  
  const totalOrderAmount = Number(document.getElementById("orderAmount").value || 0);
  const totalPaidAmount = Number(paidAmountInput || 0);
  
  if (totalOrderAmount <= 0) return alert("Please select product and enter quantity/price");
  if (totalPaidAmount < 0) return alert("Paid amount cannot be negative");
  if (totalPaidAmount > totalOrderAmount) return alert("Paid amount cannot be greater than total order amount");
  
  let remainingPaidAmount = totalPaidAmount;
  const orders = [];
  
  for (const row of rows) {
    const productId = row.querySelector(".productSelect").value;
    const orderQuantity = Number(row.querySelector(".orderQuantity").value || 0);
    const sellingPrice = Number(row.querySelector(".sellingPrice").value || 0);
    const orderAmount = Number(row.querySelector(".rowAmount").value || 0);
    
    const selectedProduct = stockList.find((p) => String(p.productId) === String(productId));
    if (!selectedProduct) return alert("Please select product in all rows");
    if (orderQuantity <= 0) return alert("Enter valid quantity for " + selectedProduct.productName);
    if (sellingPrice <= 0) return alert("Enter selling price for " + selectedProduct.productName);
    if (orderAmount <= 0) return alert("Invalid amount for " + selectedProduct.productName);
    if (orderQuantity > Number(selectedProduct.availableUnits || 0)) {
      return alert("Quantity cannot be greater than stock for " + selectedProduct.productName);
    }
    
    let rowPaidAmount = 0;
    if (remainingPaidAmount > 0) {
      rowPaidAmount = Math.min(remainingPaidAmount, orderAmount);
      remainingPaidAmount -= rowPaidAmount;
    }
    const dueAmount = orderAmount - rowPaidAmount;
    
    orders.push({
      employeeId: Number(getAsmId()),
      employeeName: getAsmName(),
      doctorId: Number(doctorId),
      doctorName: doctorName,
      visitCategory: visitCategory,
      productId: Number(productId),
      productName: selectedProduct.productName,
      orderQuantity: orderQuantity,
      sellingPrice: sellingPrice,
      orderAmount: orderAmount,
      paidAmount: rowPaidAmount,
      dueAmount: dueAmount,
      paymentMode: paymentMode,
      orderDate: getLocalDate(),
      distributorId: Number(distributorId),
      distributorName: distributorName,
      headquarters: headquarters,
      remarks: document.getElementById("remarks") ? document.getElementById("remarks").value : "",
      area: area,
    });
  }
  
  const placeBtn = document.getElementById("placeOrderBtn");
  if (placeBtn.disabled) return;
  
  placeBtn.disabled = true;
  placeBtn.innerHTML = '<i class="fa-solid fa-spinner fa-spin me-2"></i>Order Placing...';
  
  fetch(`${BASE_URL}/order/place-multiple`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(orders),
  })
    .then(async (response) => {
      const text = await response.text();
      if (!response.ok) throw new Error(text);
      alert("Orders Placed Successfully");
      
      placeBtn.disabled = false;
      placeBtn.innerHTML = '<i class="fa-solid fa-cart-shopping me-2"></i>Place Order';
      
      document.getElementById("productItems").innerHTML = "";
      productRowIndex = 0;
      addCreateProductRow();
      document.getElementById("orderAmount").value = "";
      document.getElementById("paidAmount").value = "";
      document.getElementById("dueAmount").value = "";
      document.getElementById("remarks").value = "";
      
      loadDistributorProducts();
      loadOrderHistory();
    })
    .catch((error) => {
      placeBtn.disabled = false;
      placeBtn.innerHTML = '<i class="fa-solid fa-cart-shopping me-2"></i>Place Order';
      if (error.message.includes("Insufficient stock")) alert(error.message);
      else alert("Failed to place order");
    });
}

function loadOrderHistory() {
  const empId = getAsmId();
  fetch(`${BASE_URL}/order/history/today/${empId}`)
    .then(async (response) => {
      const text = await response.text();
      if (!response.ok) throw new Error(text);
      return text ? JSON.parse(text) : [];
    })
    .then((data) => {
      historyList = data;
      const table = document.getElementById("orderTable");
      table.innerHTML = "";
      
      if (!data || data.length === 0) {
        table.innerHTML = `<tr><td colspan="13" class="text-center text-muted">No orders found</td></tr>`;
        return;
      }
      
      const groupedOrders = {};
      data.forEach((order) => {
        const key = order.orderGroupId;
        if (!groupedOrders[key]) {
          groupedOrders[key] = {
            orderGroupId: order.orderGroupId,
            doctorId: order.doctorId,
            doctorName: order.doctorName,
            visitCategory: order.visitCategory,
            distributorId: order.distributorId,
            distributorName: order.distributorName,
            orderDate: order.orderDate,
            orderTime: order.orderTime,
            status: order.status || "Placed",
            paymentMode: order.paymentMode || "-",
            remarks: order.remarks || "-",
            totalAmount: 0,
            totalPaidAmount: 0,
            totalDueAmount: 0,
            products: [],
          };
        }
        groupedOrders[key].totalAmount += Number(order.orderAmount || 0);
        groupedOrders[key].totalPaidAmount += Number(order.paidAmount || 0);
        groupedOrders[key].totalDueAmount += Number(order.dueAmount || 0);
        groupedOrders[key].products.push(`
          <div class="border-top pt-2 mt-2">
            <div class="fw-semibold">${order.productName}</div>
            <div class="small text-muted">
              Qty: ${order.orderQuantity} | SP: ₹${order.sellingPrice} | Amt: ₹${order.orderAmount}
            </div>
          </div>
        `);
      });
      
      Object.values(groupedOrders).forEach((order) => {
        table.innerHTML += `
          <tr>
            <td>${order.orderDate || "-"}</td>
            <td>${order.orderTime || "-"}</td>
            <td>${order.doctorName || "-"}</td>
            <td>${order.visitCategory === "CHEMIST" ? '<span class="badge bg-warning text-dark">Chemist</span>' : '<span class="badge bg-primary">Doctor</span>'}</td>
            <td>${order.distributorName || "-"}</td>
            <td>${order.products.join("")}</td>
            <td><strong>₹${order.totalAmount.toFixed(2)}</strong></td>
            <td class="text-success">₹${order.totalPaidAmount.toFixed(2)}</td>
            <td class="text-danger">₹${order.totalDueAmount.toFixed(2)}</td>
            <td>${order.paymentMode || "-"}</td>
            <td>${order.remarks || "-"}</td>
            <td><span class="status-placed">${order.status}</span></td>
            <td class="text-center">
                <button class="btn btn-sm btn-outline-warning" onclick="editOrder('${order.orderGroupId}')"><i class="fa-solid fa-pen"></i></button>
            </td>
          </tr>
        `;
      });
    })
    .catch((error) => {
      console.error(error);
      document.getElementById("orderTable").innerHTML = `<tr><td colspan="13" class="text-center text-danger">Failed to load order history</td></tr>`;
    });
}

function calculateDueAmount() {
  const orderAmount = Number(document.getElementById("orderAmount").value || 0);
  let paidAmount = Number(document.getElementById("paidAmount").value || 0);
  
  if (paidAmount > orderAmount) {
    alert("Paid amount cannot be greater than Total Amount");
    paidAmount = orderAmount;
    document.getElementById("paidAmount").value = orderAmount;
  }
  document.getElementById("dueAmount").value = (orderAmount - paidAmount).toFixed(2);
}

// -------------------------------------
// EDIT ORDER MODAL LOGIC
// -------------------------------------

function editOrder(groupId) {
  editingOrderGroupId = groupId;
  editingOrders = historyList.filter((o) => o.orderGroupId === groupId);
  if (editingOrders.length === 0) return alert("Order not found");
  
  const first = editingOrders[0];
  document.getElementById("editOrderGroupId").value = groupId;
  document.getElementById("editDistributor").value = first.distributorName || "";
  document.getElementById("editPaymentMode").value = first.paymentMode || "Pending";
  document.getElementById("editRemarks").value = first.remarks || "";
  
  const totalPaid = editingOrders.reduce((sum, order) => sum + Number(order.paidAmount || 0), 0);
  const totalDue = editingOrders.reduce((sum, order) => sum + Number(order.dueAmount || 0), 0);
  
  document.getElementById("editPaidAmount").value = totalPaid;
  document.getElementById("editDueAmount").value = totalDue;
  document.getElementById("submitBtn").innerHTML = '<i class="fa-solid fa-floppy-disk"></i> Save Changes';
  document.getElementById("editDoctor").value = first.doctorName || "";
  
  renderProductRows();
  loadDistributorProductsForEdit(first.distributorId);
  editOrderModal.show();
}

function renderProductRows() {
  const tbody = document.getElementById("editProductTable");
  tbody.innerHTML = "";
  editingOrders.forEach((order, index) => {
    tbody.innerHTML += `
      <tr>
        <td><select class="form-select edit-product" data-index="${index}"></select></td>
        <td><input type="number" class="form-control edit-qty" value="${order.orderQuantity}" min="1" data-index="${index}" oninput="calculateRow(${index})"></td>
        <td><input type="number" class="form-control edit-price" value="${order.sellingPrice}" min="0" step="0.01" data-index="${index}" oninput="calculateRow(${index})"></td>
        <td><input class="form-control edit-amount bg-light" value="${order.orderAmount}" readonly></td>
        <td class="text-center">
          <button class="btn btn-outline-danger btn-sm" onclick="removeEditProductRow(${index})"><i class="fa fa-trash"></i></button>
        </td>
      </tr>
    `;
  });
  fillProductDropdowns();
  updateGrandTotal();
}

function calculateRow(index) {
  const qty = Number(document.querySelectorAll(".edit-qty")[index].value);
  const price = Number(document.querySelectorAll(".edit-price")[index].value);
  const amount = qty * price;
  document.querySelectorAll(".edit-amount")[index].value = amount.toFixed(2);
  
  editingOrders[index].orderQuantity = qty;
  editingOrders[index].sellingPrice = price;
  editingOrders[index].orderAmount = amount;
  updateGrandTotal();
}

function updateGrandTotal() {
  let total = 0;
  editingOrders.forEach((order) => { total += Number(order.orderAmount || 0); });
  document.getElementById("editGrandTotal").innerText = total.toFixed(2);
  calculateEditDueAmount();
}

function addEditProductRow() {
  editingOrders.push({
    id: null,
    orderGroupId: editingOrderGroupId,
    doctorId: editingOrders[0].doctorId,
    doctorName: editingOrders[0].doctorName,
    distributorId: editingOrders[0].distributorId,
    distributorName: editingOrders[0].distributorName,
    employeeId: editingOrders[0].employeeId,
    employeeName: editingOrders[0].employeeName,
    visitCategory: editingOrders[0].visitCategory,
    orderDate: editingOrders[0].orderDate,
    orderTime: editingOrders[0].orderTime,
    paymentMode: editingOrders[0].paymentMode,
    remarks: editingOrders[0].remarks,
    productId: "",
    productName: "",
    orderQuantity: 1,
    sellingPrice: 0,
    orderAmount: 0,
  });
  renderProductRows();
}

function fillProductDropdowns() {
  const productSelects = document.querySelectorAll(".edit-product");
  productSelects.forEach((select, index) => {
    select.innerHTML = '<option value="">Select Product</option>';
    allProducts.forEach((product) => {
      const option = document.createElement("option");
      option.value = product.productId;
      option.text = `${product.productName} - ${product.availableUnits} Qty`;
      if (product.productId == editingOrders[index].productId) option.selected = true;
      select.appendChild(option);
    });
    select.onchange = function () {
      const product = allProducts.find((p) => p.productId == this.value);
      if (!product) return;
      editingOrders[index].productId = product.productId;
      editingOrders[index].productName = product.productName;
      document.querySelectorAll(".edit-price")[index].value = product.sellingPrice;
      calculateRow(index);
    };
  });
}

function loadAllProducts() {
  fetch(`${BASE_URL}/order/all`)
    .then((r) => r.json())
    .then((data) => { allProducts = data; })
    .catch(console.error);
}

function loadAllDoctors() {
  const empId = getAsmId();
  fetch(`${BASE_URL}/doctor-visit/history/today/${empId}`)
    .then((r) => r.json())
    .then((data) => { allDoctors = data; })
    .catch(console.error);
}

function loadDistributorProductsForEdit(distributorId) {
  fetch(`${BASE_URL}/distributor-stock/${distributorId}`)
    .then((r) => r.json())
    .then((data) => {
      allProducts = data;
      fillProductDropdowns();
    })
    .catch(console.error);
}

function calculateEditDueAmount() {
  const total = Number(document.getElementById("editGrandTotal").innerText);
  let paid = Number(document.getElementById("editPaidAmount").value || 0);
  if (paid > total) {
    alert("Paid amount cannot be greater than Total Amount");
    paid = total;
    document.getElementById("editPaidAmount").value = total;
  }
  document.getElementById("editDueAmount").value = (total - paid).toFixed(2);
}

function saveOrderChanges() {
  const btn = document.getElementById("submitBtn");
  btn.disabled = true;
  btn.innerHTML = `<span class="spinner-border spinner-border-sm me-2"></span>Updating...`;
  
  const paymentMode = document.getElementById("editPaymentMode").value;
  const remarks = document.getElementById("editRemarks").value;
  const paidAmount = Number(document.getElementById("editPaidAmount").value || 0);
  
  let remainingPaid = paidAmount;
  
  editingOrders.forEach((order, index) => {
    order.paymentMode = paymentMode;
    order.remarks = remarks;
    let rowPaidAmount = 0;
    let orderAmt = Number(order.orderAmount || 0);
    if (remainingPaid > 0) {
      rowPaidAmount = Math.min(remainingPaid, orderAmt);
      remainingPaid -= rowPaidAmount;
    }
    order.paidAmount = rowPaidAmount;
    order.dueAmount = orderAmt - rowPaidAmount;
  });
  
  document.querySelectorAll(".edit-product").forEach((select, index) => {
    const product = allProducts.find((p) => p.productId == select.value);
    if (product) {
      editingOrders[index].productId = product.productId;
      editingOrders[index].productName = product.productName;
    }
  });
  
  fetch(`${BASE_URL}/order/update-group/${editingOrderGroupId}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(editingOrders),
  })
    .then(async (response) => {
      const text = await response.text();
      if (!response.ok) throw new Error(text);
      editOrderModal.hide();
      loadOrderHistory();
      alert("Order Updated Successfully");
      btn.disabled = false;
      btn.innerHTML = '<i class="fa-solid fa-floppy-disk me-2"></i> Save Changes';
    })
    .catch((error) => {
      console.error(error);
      alert(error.message);
      btn.disabled = false;
      btn.innerHTML = '<i class="fa-solid fa-floppy-disk me-2"></i> Save Changes';
    });
}

function clearEditModal() {
  editingOrders = [];
  editingOrderGroupId = "";
  document.getElementById("editProductTable").innerHTML = "";
  document.getElementById("editGrandTotal").innerText = "0";
}

document.getElementById("editOrderModal").addEventListener("hidden.bs.modal", clearEditModal);

function removeEditProductRow(index) {
  if (editingOrders.length == 1) return alert("At least one product is required.");
  if (!confirm("Remove this product?")) return;
  editingOrders.splice(index, 1);
  renderProductRows();
}