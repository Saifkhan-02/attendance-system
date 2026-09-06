const KM_RATE = 2.2;
let routeList = [];
let employeeHQ = "";

window.onload = function () {
  document.getElementById("employeeId").value = localStorage.getItem("employeeId") || "";
  document.getElementById("employeeName").value = localStorage.getItem("employeeName") || "";

  // Local time ke hisab se current year aur month nikalna
  const now = new Date();
  const localYear = now.getFullYear();
  const localMonth = String(now.getMonth() + 1).padStart(2, '0'); // Month 0-11 hota hai, isliye +1 kiya
  const currentMonthStr = `${localYear}-${localMonth}`; // Output: "2026-09"

  document.getElementById("planMonth").value = currentMonthStr;
  document.getElementById("monthFilter").value = currentMonthStr;

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

      // Default first HQ select karlo agar available ho
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
  if (!employeeHQ) {
    routeList = [];
    document.getElementById("monthlyPlanTable").innerHTML = `
      <tr><td colspan="9" class="text-center text-muted">Please select a headquarter</td></tr>
    `;
    return;
  }

  // Fetch active routes for the selected HQ
  fetch(`${BASE_URL}/asm/routes?headquarterName=${encodeURIComponent(employeeHQ)}`)
    .then(res => res.json())
    .then(routes => {
      routeList = routes || [];
      generateMonthlyRows(); // Iske andar automatically loadSavedMonthlyPlan() call ho jayega
    })
    .catch(err => {
      console.error(err);
      alert("Failed to load routes for this HQ");
    });
}
// 3. Generate Calendar Rows for the Selected Month with Route Dropdowns
function generateMonthlyRows() {
  const month = document.getElementById("planMonth").value;
  const table = document.getElementById("monthlyPlanTable");

  if (!month || !employeeHQ) return;

  table.innerHTML = `
    <tr>
      <td colspan="9" class="text-center text-muted">Generating schedule...</td>
    </tr>
  `;

  // Get days in the selected month
  const [year, m] = month.split("-");
  const daysInMonth = new Date(year, m, 0).getDate();
  
  table.innerHTML = "";

  const daysNames = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"];

  for (let day = 1; day <= daysInMonth; day++) {
    const dayStr = String(day).padStart(2, "0");
    const tourDate = `${month}-${dayStr}`;
    const dateObj = new Date(tourDate);
    const dayName = daysNames[dateObj.getDay()];

    // Dropdown options for routes
    let routeOptions = '<option value="">Select Route</option>';
    routeList.forEach(r => {
      routeOptions += `<option value="${r.routeName}" data-km="${r.distanceKm || r.distance || 0}">${r.routeName} (${r.distanceKm || r.distance || 0} KM)</option>`;
    });

    table.innerHTML += `
      <tr class="plan-row" data-date="${tourDate}" data-weekday="${dayName}">
        <td>${tourDate}</td>
        <td>${dayName}</td>
        <td>
          <select class="form-select routeSelect" onchange="onRouteSelect(this)">
            ${routeOptions}
          </select>
        </td>
        <td><input type="number" class="form-control distanceKm" readonly value="0"></td>
        <td><input type="number" class="form-control fareAmount" readonly step="0.1" value="0"></td>
        <td><input type="number" class="form-control daAmount" value="0" oninput="updateTotals()"></td>
        <td><input type="number" class="form-control otherAmount" value="0" oninput="updateTotals()"></td>
        <td><input type="number" class="form-control totalExpenseAmount" readonly step="0.1" value="0"></td>
        <td>
          <button class="btn btn-primary btn-sm submitDayBtn" onclick="submitSingleDayPlan(this)">
            <i class="fa-solid fa-paper-plane me-1"></i> Submit
          </button>
        </td>
      </tr>
    `;
  }

  loadSavedMonthlyPlan();
}
// 4. Update KM and Fare automatically when ASM selects a route from dropdown
function onRouteSelect(selectElement) {
  const row = selectElement.closest(".plan-row");
  const selectedOption = selectElement.options[selectElement.selectedIndex];
  const km = Number(selectedOption.getAttribute("data-km") || 0);
  const fare = Number((km * KM_RATE).toFixed(1));

  row.querySelector(".distanceKm").value = km;
  row.querySelector(".fareAmount").value = fare;

  updateTotals();
  updateCoverage();
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
        const row = document.querySelector(`.plan-row[data-date="${plan.travelDate}"]`);
        if (!row) return;

        // Agar route dropdown me woh route available nahi hai (kyunki doosre HQ ka hai), 
        // toh usko dynamically option me add kar denge taaki naam dikh sake
        const routeDropdown = row.querySelector(".routeSelect");
        let optionExists = Array.from(routeDropdown.options).some(opt => opt.value === plan.routeName);
        if (!optionExists && plan.routeName) {
          routeDropdown.innerHTML += `<option value="${plan.routeName}">${plan.routeName}</option>`;
        }

        routeDropdown.value = plan.routeName;
        row.querySelector(".distanceKm").value = Number(plan.travelKm || 0).toFixed(1);
        row.querySelector(".fareAmount").value = Number(plan.fareAmount || 0).toFixed(1);
        row.querySelector(".daAmount").value = Number(plan.daAmount || 0).toFixed(1);
        row.querySelector(".otherAmount").value = Number(plan.otherAmount || 0).toFixed(1);

        const savedTotal =
          Number(plan.fareAmount || 0) +
          Number(plan.daAmount || 0) +
          Number(plan.otherAmount || 0);

        row.querySelector(".totalExpenseAmount").value = savedTotal.toFixed(1);

        // Disable button & mark completed
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
    const routeName = row.querySelector(".routeSelect").value;
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

  document.getElementById("coverageText").innerText = `${selectedRoutes.length} tour days planned`;
  document.getElementById("coverageList").innerHTML = html;
}
function submitSingleDayPlan(button) {
  const row = button.closest(".plan-row");
  const routeDropdown = row.querySelector(".routeSelect");
  const routeName = routeDropdown.value;

  if (!routeName) {
    alert("Please select a route for this date");
    routeDropdown.focus();
    return;
  }

  const da = Number(row.querySelector(".daAmount").value || 0);
  if (da <= 0) {
    alert("Please enter D.A amount");
    row.querySelector(".daAmount").focus();
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
        alert("Today's tour has already been submitted");
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