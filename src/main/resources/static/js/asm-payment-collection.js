// ==========================================
// ASM PAYMENT COLLECTION JAVASCRIPT
// ==========================================

let allOrders = [];
let selectedDoctorOrders = [];

// Safe ID fetch for ASM
function getAsmId() {
  return localStorage.getItem("asmId") || localStorage.getItem("employeeId");
}

window.onload = function () {
  if (typeof checkAsmSession === "function") {
    checkAsmSession();
  }
  loadEmployeeOrders();
};

function loadEmployeeOrders() {
  const empId = getAsmId();

  fetch(`${BASE_URL}/order/history/${empId}`)
    .then(res => res.json())
    .then(data => {
      allOrders = data || [];
      loadDoctorDropdown();
    })
    .catch(error => {
      console.error(error);
      alert("Failed to load orders");
    });
}

function loadDoctorDropdown() {
  const dropdown = document.getElementById("doctorSelect");
  dropdown.innerHTML = `<option value="">Select Doctor</option>`;

  const dueOrders = allOrders.filter(o => Number(o.dueAmount || 0) > 0);
  const doctorMap = new Map();

  dueOrders.forEach(order => {
    if (order.doctorId) {
      doctorMap.set(order.doctorId, order.doctorName);
    }
  });

  doctorMap.forEach((name, id) => {
    dropdown.innerHTML += `<option value="${id}">${name}</option>`;
  });
}

function showDoctorDue() {
  const doctorId = document.getElementById("doctorSelect").value;

  selectedDoctorOrders = allOrders.filter(order => String(order.doctorId) === String(doctorId));

  const sale = selectedDoctorOrders.reduce((s, o) => s + Number(o.orderAmount || 0), 0);
  const paid = selectedDoctorOrders.reduce((s, o) => s + Number(o.paidAmount || 0), 0);
  const due = selectedDoctorOrders.reduce((s, o) => s + Number(o.dueAmount || 0), 0);

  document.getElementById("totalSale").innerText = formatRupees(sale);
  document.getElementById("totalPaid").innerText = formatRupees(paid);
  document.getElementById("totalDue").innerText = formatRupees(due);
}

function updateDoctorPayment() {
  const doctorId = document.getElementById("doctorSelect").value;
  const paymentMode = document.getElementById("paymentMode").value;
  const receivedAmount = Number(document.getElementById("receivedAmount").value || 0);

  if (!doctorId) return alert("Please select doctor");
  if (!paymentMode) return alert("Please select payment mode");
  if (receivedAmount <= 0) return alert("Please enter valid received amount");

  const totalDue = selectedDoctorOrders.reduce((s, o) => s + Number(o.dueAmount || 0), 0);

  if (receivedAmount > totalDue) {
    return alert("Received amount cannot be greater than due amount");
  }

  let remainingAmount = receivedAmount;
  const pendingOrders = selectedDoctorOrders
    .filter(o => Number(o.dueAmount || 0) > 0)
    .sort((a, b) => Number(a.id) - Number(b.id));

  const btn = document.getElementById("updatePaymentBtn");
  btn.disabled = true;
  btn.innerHTML = `<i class="fa-solid fa-spinner fa-spin me-2"></i>Updating...`;

  const requests = [];

  for (const order of pendingOrders) {
    if (remainingAmount <= 0) break;

    const orderDue = Number(order.dueAmount || 0);
    const payNow = Math.min(remainingAmount, orderDue);
    remainingAmount -= payNow;

    requests.push(
      fetch(`${BASE_URL}/order/collect-payment/${order.id}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          receivedAmount: payNow,
          paymentMode: paymentMode,
          remarks: document.getElementById("remarks").value || ""
        })
      })
    );
  }

  Promise.all(requests)
    .then(async responses => {
      for (const res of responses) {
        if (!res.ok) throw new Error(await res.text());
      }

      alert("Payment updated successfully");
      document.getElementById("receivedAmount").value = "";
      document.getElementById("remarks").value = "";
      document.getElementById("paymentMode").value = "";

      loadEmployeeOrders();
      resetSummary();
    })
    .catch(error => {
      console.error(error);
      alert("Failed to update payment");
    })
    .finally(() => {
      btn.disabled = false;
      btn.innerHTML = `<i class="fa-solid fa-check me-2"></i> Update Payment`;
    });
}

function resetSummary() {
  document.getElementById("totalSale").innerText = "₹0";
  document.getElementById("totalPaid").innerText = "₹0";
  document.getElementById("totalDue").innerText = "₹0";
}

function formatRupees(amount) {
  return "₹" + Number(amount || 0).toLocaleString("en-IN");
}