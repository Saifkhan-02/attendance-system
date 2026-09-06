// ==========================================
// ASM SALES & PAYMENT JAVASCRIPT
// ==========================================

let salesChart;
let paymentChart;

// Safe ID fetch for ASM
function getAsmId() {
  return localStorage.getItem("asmId") || localStorage.getItem("employeeId");
}

window.onload = function () {
  if (typeof checkAsmSession === "function") {
    checkAsmSession();
  }
  loadDistributorsForFilter();
  loadDoctorsForFilter();
  loadFilteredSalesData();
};

function applySalesFilter() {
  loadFilteredSalesData();
}

function loadDistributorsForFilter() {
  const empId = getAsmId();

  fetch(`${BASE_URL}/employee/${empId}`)
    .then((response) => response.json())
    .then((employee) => {
      const headquarters = employee.headquarters || employee.headQuarters || employee.headQuarter || "";
      return fetch(`${BASE_URL}/distributor/by-headquarter/${encodeURIComponent(headquarters)}`);
    })
    .then((response) => response.json())
    .then((data) => {
      const dropdown = document.getElementById("distributorFilter");
      dropdown.innerHTML = `<option value="all">All Distributors</option>`;
      data.forEach((distributor) => {
        dropdown.innerHTML += `<option value="${distributor.distributorName}">${distributor.distributorName}</option>`;
      });
    })
    .catch((error) => console.error("DISTRIBUTOR FILTER ERROR:", error));
}

function loadDoctorsForFilter() {
  const empId = getAsmId();

  fetch(`${BASE_URL}/doctor-visit/unique-doctors/${empId}`)
    .then((response) => response.json())
    .then((data) => {
      const dropdown = document.getElementById("doctorFilter");
      dropdown.innerHTML = `<option value="all">All Doctors</option>`;
      data.forEach((doctor) => {
        dropdown.innerHTML += `<option value="${doctor.id}">${doctor.doctorName}</option>`;
      });
    })
    .catch((error) => {
      console.error(error);
      alert("Failed to load doctors");
    });
}

function loadFilteredSalesData() {
  const empId = getAsmId();
  const doctorId = document.getElementById("doctorFilter").value;
  const distributorName = document.getElementById("distributorFilter").value;
  
  // Date Filters
  const fromDate = document.getElementById("fromDate").value;
  const toDate = document.getElementById("toDate").value;

  fetch(`${BASE_URL}/order/history/${empId}`)
    .then((response) => response.json())
    .then((data) => {
      let filtered = data || [];

      // Filter by Doctor
      if (doctorId !== "all" && doctorId !== "") {
        filtered = filtered.filter((order) => String(order.doctorId) === String(doctorId));
      }

      // Filter by Distributor
      if (distributorName !== "all" && distributorName !== "") {
        filtered = filtered.filter((order) => order.distributorName === distributorName);
      }

      // Filter by Date (Added logic to make date inputs functional)
      if (fromDate) {
        filtered = filtered.filter((order) => order.orderDate >= fromDate);
      }
      if (toDate) {
        filtered = filtered.filter((order) => order.orderDate <= toDate);
      }

      renderSummaryFromOrders(filtered);
      renderDailySalesTable(filtered);
      renderDoctorPaymentTableFromOrders(filtered);
      renderChartsFromOrders(filtered);
    })
    .catch((error) => console.error("Error loading filtered sales:", error));
}

function renderSummaryFromOrders(orders) {
  // Local time (IST) ke hisab se aaj ki date aur month nikalna
  const now = new Date();
  const year = now.getFullYear();
  const month = String(now.getMonth() + 1).padStart(2, '0');
  const date = String(now.getDate()).padStart(2, '0');

  const today = `${year}-${month}-${date}`;       // Output: "2026-09-01"
  const currentMonth = `${year}-${month}`;        // Output: "2026-09"

  const todaySale = orders
    .filter((o) => o.orderDate === today)
    .reduce((sum, o) => sum + Number(o.orderAmount || 0), 0);

  const monthlySale = orders
    .filter((o) => o.orderDate && o.orderDate.startsWith(currentMonth))
    .reduce((sum, o) => sum + Number(o.orderAmount || 0), 0);

  const paid = orders.reduce((sum, o) => sum + Number(o.paidAmount || 0), 0);
  const due = orders.reduce((sum, o) => sum + Number(o.dueAmount || 0), 0);

  document.getElementById("todaySale").innerText = "₹" + todaySale.toLocaleString("en-IN");
  document.getElementById("monthlySale").innerText = "₹" + monthlySale.toLocaleString("en-IN");
  document.getElementById("paymentReceived").innerText = "₹" + paid.toLocaleString("en-IN");
  document.getElementById("paymentDue").innerText = "₹" + due.toLocaleString("en-IN");
}

function renderDailySalesTable(orders) {
  const table = document.getElementById("dailySalesTable");
  table.innerHTML = "";

  if (!orders || orders.length === 0) {
    table.innerHTML = `<tr><td colspan="7" class="text-center text-muted">No records found</td></tr>`;
    return;
  }

  orders.forEach((order) => {
    table.innerHTML += `
      <tr>
        <td>${order.orderDate || "-"}</td>
        <td>${order.distributorName || "-"}</td>
        <td>${order.doctorName || "-"}</td>
        <td class="fw-bold">₹${order.orderAmount || 0}</td>
        <td class="text-success">₹${order.paidAmount || 0}</td>
        <td class="text-danger">₹${order.dueAmount || 0}</td>
        <td>${order.paymentMode || "Pending"}</td>
      </tr>
    `;
  });
}

function renderDoctorPaymentTableFromOrders(orders) {
  const table = document.getElementById("doctorPaymentTable");
  table.innerHTML = "";

  if (!orders || orders.length === 0) {
    table.innerHTML = `<tr><td colspan="5" class="text-center text-muted">No data found</td></tr>`;
    return;
  }

  const grouped = {};

  orders.forEach((order) => {
    const doctor = order.doctorName || "-";
    if (!grouped[doctor]) {
      grouped[doctor] = { totalSale: 0, paymentReceived: 0, paymentDue: 0 };
    }
    grouped[doctor].totalSale += Number(order.orderAmount || 0);
    grouped[doctor].paymentReceived += Number(order.paidAmount || 0);
    grouped[doctor].paymentDue += Number(order.dueAmount || 0);
  });

  Object.keys(grouped).forEach((doctor) => {
    const item = grouped[doctor];
    let status = "Paid";
    let badgeClass = "badge-paid";

    if (item.paymentDue > 0 && item.paymentReceived > 0) {
      status = "Partial";
      badgeClass = "badge-partial";
    } else if (item.paymentDue > 0) {
      status = "Due";
      badgeClass = "badge-due";
    }

    table.innerHTML += `
      <tr>
        <td class="fw-semibold">${doctor}</td>
        <td>₹${item.totalSale.toLocaleString("en-IN")}</td>
        <td class="text-success">₹${item.paymentReceived.toLocaleString("en-IN")}</td>
        <td class="text-danger">₹${item.paymentDue.toLocaleString("en-IN")}</td>
        <td><span class="${badgeClass}">${status}</span></td>
      </tr>
    `;
  });
}

function renderChartsFromOrders(orders) {
  const grouped = {};

  orders.forEach((order) => {
    const date = order.orderDate || "-";
    if (!grouped[date]) {
      grouped[date] = { sale: 0, paid: 0, due: 0 };
    }
    grouped[date].sale += Number(order.orderAmount || 0);
    grouped[date].paid += Number(order.paidAmount || 0);
    grouped[date].due += Number(order.dueAmount || 0);
  });

  const labels = Object.keys(grouped).sort();
  const sales = labels.map((date) => grouped[date].sale);
  const paid = labels.map((date) => grouped[date].paid);
  const due = labels.map((date) => grouped[date].due);

  // Render Bar Chart
  if (salesChart) salesChart.destroy();
  salesChart = new Chart(document.getElementById("salesChart"), {
    type: "bar",
    data: {
      labels: labels,
      datasets: [{
        label: "Daily Sale",
        data: sales,
        backgroundColor: "#2b8fc6",
        borderRadius: 8,
      }],
    },
    options: {
      responsive: true,
      plugins: { legend: { display: false } },
    },
  });

  // Render Doughnut Chart
  const totalPaid = paid.reduce((a, b) => a + b, 0);
  const totalDue = due.reduce((a, b) => a + b, 0);

  if (paymentChart) paymentChart.destroy();
  paymentChart = new Chart(document.getElementById("paymentChart"), {
    type: "doughnut",
    data: {
      labels: ["Received", "Due"],
      datasets: [{
        data: [totalPaid, totalDue],
        backgroundColor: ["#22c55e", "#ef4444"],
      }],
    },
    options: {
      responsive: true,
    },
  });
}