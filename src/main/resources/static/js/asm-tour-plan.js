const KM_RATE = 2.2;
let routeList = [];
let employeeHQ = "";

window.onload = function () {
  document.getElementById("employeeId").value = localStorage.getItem("employeeId") || "";
  document.getElementById("employeeName").value = localStorage.getItem("employeeName") || "";

  document.getElementById("planMonth").value = new Date().toISOString().slice(0, 7);
  document.getElementById("monthFilter").value = new Date().toISOString().slice(0, 7);

  loadASMHeadquarters();
};

// 1. Load Assigned HQs for ASM Dropdown
function loadASMHeadquarters() {
  const employeeId = localStorage.getItem("employeeId");

  fetch(`${BASE_URL}/asm/${employeeId}/headquarters`)
    .then(res => res.json())
    .then(hqs => {
      const dropdown = document.getElementById("hqDropdown");
      dropdown.innerHTML = '<option value="">Select HQ</option>';

      if (!hqs || hqs.length === 0) {
        alert("No headquarter assigned to you. Please contact admin.");
        return;
      }

      hqs.forEach(hq => {
        dropdown.innerHTML += `<option value="${hq.headquarterName}">${hq.headquarterName}</option>`;
      });

      // By default first HQ select karke routes load kar lo
      if (hqs.length > 0) {
        dropdown.value = hqs[0].headquarterName;
        onHeadquarterChange();
      }
    })
    .catch(err => {
      console.error(err);
      alert("Failed to load assigned headquarters");
    });
}

// 2. Triggered when ASM changes HQ from dropdown
function onHeadquarterChange() {
  employeeHQ = document.getElementById("hqDropdown").value;
  if (!employeeHQ) return;

  // Load routes for the selected HQ
  fetch(`${BASE_URL}/route-master/active/by-headquarter/${encodeURIComponent(employeeHQ)}`)
    .then(res => res.json())
    .then(routes => {
      routeList = routes || [];
      generateMonthlyRows();
      loadTourHistory();
    })
    .catch(err => {
      console.error(err);
      alert("Failed to load routes for this HQ");
    });
}
function generateMonthlyRows() {
  const employeeId = localStorage.getItem("employeeId");
  const month = document.getElementById("planMonth").value;
  const table = document.getElementById("monthlyPlanTable");

  if (!month) return;

  table.innerHTML = `
    <tr>
      <td colspan="9" class="text-center text-muted">Loading assigned routes...</td>
    </tr>
  `;

  fetch(`${BASE_URL}/tour-assignment/${employeeId}/${month}`)
    .then(res => res.json())
    .then(assignments => {
      table.innerHTML = "";

      if (!assignments || assignments.length === 0) {
        table.innerHTML = `
          <tr>
            <td colspan="9" class="text-center text-danger">
              No route assigned for this month. Please contact admin.
            </td>
          </tr>
        `;
        return;
      }

      assignments.forEach(item => {
        const route = routeList.find(r =>
          (r.routeName || "").trim().toLowerCase() ===
          (item.routeName || "").trim().toLowerCase()
        );

        const km = route ? Number(route.distanceKm || route.distance || 0) : 0;
        const fare = Number((km * KM_RATE).toFixed(1));

        table.innerHTML += `
          <tr class="plan-row"
              data-date="${item.tourDate}"
              data-weekday="${item.dayName}"
              data-route="${item.routeName}"
              data-km="${km}"
              data-fare="${fare}">
            <td>${item.tourDate}</td>
            <td>${item.dayName}</td>
            <td>
              <input type="text"
                     class="form-control routeName"
                     value="${item.routeName}"
                     readonly>
            </td>
            <td><input type="number" class="form-control distanceKm" readonly value="${km}"></td>
            <td><input type="number" class="form-control fareAmount" readonly step="0.1" value="${fare}"></td>
            <td><input type="number" class="form-control daAmount" value="0" oninput="updateTotals()"></td>
            <td><input type="number" class="form-control otherAmount" value="0" oninput="updateTotals()"></td>
            <td><input type="number" class="form-control totalExpenseAmount" readonly step="0.1"></td>
            <td>
              <button class="btn btn-primary btn-sm submitDayBtn"
                      onclick="submitSingleDayPlan(this)">
                <i class="fa-solid fa-paper-plane me-1"></i>
                Submit
              </button>
            </td>
          </tr>
        `;
      });

      updateCoverage();
      loadSavedMonthlyPlan();
      loadTourSummary();
    })
    .catch(error => {
      console.error(error);
      table.innerHTML = `
        <tr>
          <td colspan="9" class="text-center text-danger">
            Failed to load assigned routes
          </td>
        </tr>
      `;
    });
}

function updateTotals() {
  document.querySelectorAll(".plan-row").forEach(row => {
    const fare = Number(row.querySelector(".fareAmount").value || 0);
    const da = Number(row.querySelector(".daAmount").value || 0);
    const other = Number(row.querySelector(".otherAmount").value || 0);

    const totalExpense = fare + da + other;

    const totalInput = row.querySelector(".totalExpenseAmount");
    if (totalInput) {
      totalInput.value = totalExpense.toFixed(1);
    }
  });
}

function loadSavedMonthlyPlan() {
  const employeeId = localStorage.getItem("employeeId");
  const month = document.getElementById("planMonth").value;

  if (!month) return;

  fetch(`${BASE_URL}/tour-plan/monthly/history/${employeeId}/${month}`)
    .then(res => res.json())
    .then(data => {
      const submittedPlans = data || [];

      submittedPlans.forEach(plan => {
        const row = document.querySelector(
          `.plan-row[data-date="${plan.travelDate}"]`
        );

        if (!row) return;

        row.querySelector(".distanceKm").value = Number(plan.travelKm || 0).toFixed(1);
        row.querySelector(".fareAmount").value = Number(plan.fareAmount || 0).toFixed(1);
        row.querySelector(".daAmount").value = Number(plan.daAmount || 0).toFixed(1);
        row.querySelector(".otherAmount").value = Number(plan.otherAmount || 0).toFixed(1);

        const savedTotal =
          Number(plan.fareAmount || 0) +
          Number(plan.daAmount || 0) +
          Number(plan.otherAmount || 0);

        row.querySelector(".totalExpenseAmount").value = savedTotal.toFixed(1);

        const btn = row.querySelector(".submitDayBtn");
        if (btn) {
          btn.disabled = true;
          btn.className = "btn btn-success btn-sm submitDayBtn";
          btn.innerHTML = `<i class="fa-solid fa-check me-1"></i>Completed`;
        }
      });

      updateCoverage();
      loadTourSummary();
      loadTourHistory();
    })
    .catch(err => console.error("Saved monthly plan error:", err));
}

function loadTourSummary() {
  const employeeId = localStorage.getItem("employeeId");
  const month = document.getElementById("planMonth").value;

  if (!employeeId || !month) return;

  fetch(`${BASE_URL}/tour-plan/summary/${employeeId}/${month}`)
    .then(res => res.json())
    .then(data => {
      document.getElementById("totalKm").innerText = Number(data.totalKm || 0).toFixed(1);
      document.getElementById("totalFare").innerText = "₹" + Number(data.totalFare || 0).toFixed(1);
      document.getElementById("totalDa").innerText = "₹" + Number(data.totalDa || 0).toFixed(1);
      document.getElementById("totalExpense").innerText = "₹" + Number(data.totalExpense || 0).toFixed(1);
    })
    .catch(err => console.error("Tour summary error:", err));
}

function updateCoverage() {
  const selectedRoutes = [];

  document.querySelectorAll(".plan-row").forEach(row => {
    const routeName = row.dataset.route;
    if (routeName) selectedRoutes.push(routeName);
  });

  const routeCount = {};
  selectedRoutes.forEach(name => {
    routeCount[name] = (routeCount[name] || 0) + 1;
  });

  let html = "";
  Object.keys(routeCount).forEach(route => {
    html += `
      <span class="badge bg-primary">
        ${route} - ${routeCount[route]} time${routeCount[route] > 1 ? "s" : ""}
      </span>
    `;
  });

  document.getElementById("coverageText").innerText = `${selectedRoutes.length} assigned tour days`;
  document.getElementById("coverageList").innerHTML = html;
}

function submitSingleDayPlan(button) {
  const row = button.closest(".plan-row");
  const routeName = row.dataset.route;

  if (!routeName) {
    alert("No route assigned for this date");
    return;
  }

  const da = Number(row.querySelector(".daAmount").value || 0);
  if (da <= 0) {
    alert("Please enter D.A amount");
    row.querySelector(".daAmount").focus();
    return;
  }

  const other = Number(row.querySelector(".otherAmount").value || 0);
  if (other < 0) {
    alert("Other Amount cannot be negative");
    return;
  }

  const selectedMonth = document.getElementById("planMonth").value;
  const currentMonth = new Date().toISOString().slice(0, 7);

  if (selectedMonth !== currentMonth) {
    alert("Only current month tour can be submitted");
    return;
  }

  const planDate = row.dataset.date;
  const today = new Date();
  const todayDate = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, "0")}-${String(today.getDate()).padStart(2, "0")}`;

  if (planDate !== todayDate) {
    alert("Only today's assigned tour can be submitted");
    return;
  }

  const travelKm = Number(Number(row.querySelector(".distanceKm").value || 0).toFixed(1));
  const fareAmount = Number(Number(row.querySelector(".fareAmount").value || 0).toFixed(1));
  const daAmount = Number(Number(row.querySelector(".daAmount").value || 0).toFixed(1));
  const otherAmount = Number(Number(row.querySelector(".otherAmount").value || 0).toFixed(1));
  const totalExpense = Number((fareAmount + daAmount + otherAmount).toFixed(1));

  const tourData = {
    employeeId: Number(localStorage.getItem("employeeId")),
    employeeName: localStorage.getItem("employeeName"),
    headquarter: employeeHQ,
    month: document.getElementById("planMonth").value,
    travelDate: row.dataset.date,
    weekDay: row.dataset.weekday,
    routeName: routeName,
    travelKm: travelKm,
    fareAmount: fareAmount,
    daAmount: daAmount,
    otherAmount: otherAmount,
    totalExpense: totalExpense,
    status: "COMPLETED"
  };

  fetch(`${BASE_URL}/tour-plan/save`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify(tourData)
  })
    .then(async response => {
      const text = await response.text();
      if (!response.ok) {
        throw new Error(text);
      }

      button.disabled = true;
      button.className = "btn btn-success btn-sm";
      button.innerHTML = `<i class="fa-solid fa-check me-1"></i>Completed`;

      loadSavedMonthlyPlan();
      loadTourSummary();
      loadTourHistory();
    })
    .catch(error => {
      console.error(error);
      if (error.message.includes("already submitted")) {
        alert("Today tour has already submitted");
      } else {
        alert("Failed to submit day tour");
      }
    });
}

function loadTourHistory() {
  const employeeId = localStorage.getItem("employeeId");
  const month = document.getElementById("monthFilter").value || document.getElementById("planMonth").value;

  fetch(`${BASE_URL}/tour-plan/monthly/history/${employeeId}/${month}`)
    .then(res => res.json())
    .then(data => {
      const table = document.getElementById("tourHistoryTable");
      table.innerHTML = "";

      if (!data || data.length === 0) {
        table.innerHTML = `
          <tr>
            <td colspan="10" class="text-center text-muted">No visited tour found</td>
          </tr>
        `;
        return;
      }

      data.forEach(tour => {
        table.innerHTML += `
          <tr>
            <td>${tour.employeeName || "-"}</td>
            <td>${tour.travelDate || "-"}</td>
            <td>${tour.weekDay || "-"}</td>
            <td>${tour.headquarter || "-"}</td>
            <td>${tour.routeName || "-"}</td>
            <td>${Number(tour.travelKm || 0).toFixed(1)}</td>
            <td>₹${Number(tour.fareAmount || 0).toFixed(1)}</td>
            <td>₹${Number(tour.daAmount || 0).toFixed(1)}</td>
            <td>₹${Number(tour.otherAmount || 0).toFixed(1)}</td>
            <td>₹${Number(tour.totalExpense || 0).toFixed(1)}</td>
          </tr>
        `;
      });
    });
}