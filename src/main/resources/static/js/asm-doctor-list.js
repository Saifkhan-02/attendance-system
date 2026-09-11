// ==========================================
// ASM DOCTOR & CHEMIST LIST JAVASCRIPT (High Performance)
// ==========================================

let uniqueDoctors = [];
let currentDoctorList = [];
let selectedCategory = "DOCTOR";
let currentDisplayLimit = 50; // Ek baar me 50 dikhayenge

function getAsmId() {
  return localStorage.getItem("asmId") || localStorage.getItem("employeeId");
}

window.onload = function () {
  if (typeof checkAsmSession === "function") {
    checkAsmSession();
  }
  loadAsmRoutes(); 
  loadTeamDoctors();
};

function getSelectedCategory() {
  const selected = document.querySelector('input[name="listCategory"]:checked');
  return selected ? selected.value : "DOCTOR";
}

function changeListCategory() {
  selectedCategory = getSelectedCategory();
  document.getElementById("searchDoctor").value = "";
  document.getElementById("mrFilter").value = "";
  document.getElementById("routeFilter").value = "";

  if (selectedCategory === "CHEMIST") {
    document.getElementById("pageTitle").innerText = "Team's Chemist List";
    document.getElementById("pageSubtitle").innerText = "List of all chemists visited by your MRs";
    document.getElementById("searchDoctor").placeholder = "Search chemist by name or area...";
  } else {
    document.getElementById("pageTitle").innerText = "Team's Doctor List";
    document.getElementById("pageSubtitle").innerText = "List of all doctors visited by your MRs";
    document.getElementById("searchDoctor").placeholder = "Search doctor by name or area...";
  }

  loadTeamDoctors();
}

function loadAsmRoutes() {
  fetch(`${BASE_URL}/asm/routes/${getAsmId()}`)
    .then((res) => res.json())
    .then((data) => {
      const routeSelect = document.getElementById("routeFilter");
      routeSelect.innerHTML = '<option value="">All Routes</option>';
      data.forEach((r) => {
        routeSelect.innerHTML += `<option value="${r.routeName}">${r.routeName} (${r.headquarterName})</option>`;
      });
    })
    .catch((err) => console.error("Error loading ASM routes:", err));
}

// 🚀 FIXED: Sequential Loading to save Backend from Crashing
async function loadTeamDoctors() {
  const asmId = getAsmId();
  const container = document.getElementById("doctorList");
  selectedCategory = getSelectedCategory();
  
  container.innerHTML = `
    <div class="col-12 text-center py-4">
      <div class="spinner-border text-primary"></div>
      <div class="mt-2 text-muted fw-bold" id="loadingProgress">Connecting to server...</div>
    </div>
  `;

  try {
    let teamIds = [asmId]; 
    
    // 1. Fetch Team
    try {
      const teamResponse = await fetch(`${BASE_URL}/asm/team/${asmId}`);
      if (teamResponse.ok) {
        const teamData = await teamResponse.json();
        teamData.forEach(emp => {
          if (emp.id) teamIds.push(emp.id); 
        });
      }
    } catch (e) {
      console.warn("Could not fetch team, loading only ASM data");
    }

    let combinedDoctors = [];
    const loadingText = document.getElementById("loadingProgress");

    // 2. Fetch data ONE BY ONE (Sequentially) taaki DB overload na ho
    for (let i = 0; i < teamIds.length; i++) {
      if(loadingText) {
        loadingText.innerText = `Fetching data... (${i + 1} of ${teamIds.length} MRs)`;
      }
      
      try {
        const res = await fetch(`${BASE_URL}/doctor-visit/unique-parties/${teamIds[i]}?category=${encodeURIComponent(selectedCategory)}`);
        if(res.ok) {
          const mrDoctors = await res.json();
          if(mrDoctors && mrDoctors.length > 0) {
            combinedDoctors = combinedDoctors.concat(mrDoctors);
          }
        }
      } catch (err) {
        console.error(`Failed to load for MR ${teamIds[i]}`);
      }
    }

    // 3. Deduplicate 1500+ records safely
    if(loadingText) loadingText.innerText = "Processing records...";
    
    const doctorMap = new Map();
    combinedDoctors.forEach(doc => {
      const key = (doc.doctorName || "").toLowerCase() + "-" + (doc.hospitalName || "").toLowerCase();
      if (!doctorMap.has(key)) {
        doctorMap.set(key, doc);
      } else {
        const existing = doctorMap.get(key);
        if (new Date(doc.visitDate) > new Date(existing.visitDate)) {
          doctorMap.set(key, doc);
        }
      }
    });

    uniqueDoctors = Array.from(doctorMap.values());

    populateFilters();
    applyFilters(); // Isme display calling hogi

  } catch (error) {
    console.error("Error loading team doctors:", error);
    container.innerHTML = `<div class="col-12"><div class="alert alert-danger text-center">Failed to load records</div></div>`;
  }
}

function populateFilters() {
  const mrDropdown = document.getElementById("mrFilter");
  const mrs = [...new Set(uniqueDoctors.map(d => d.employeeName).filter(Boolean))].sort();

  mrDropdown.innerHTML = '<option value="">All MRs (Employees)</option>';
  mrs.forEach(mr => {
    mrDropdown.innerHTML += `<option value="${mr}">${mr}</option>`;
  });
}

function applyFilters() {
  const keyword = document.getElementById("searchDoctor").value.trim().toLowerCase();
  const selectedMr = document.getElementById("mrFilter").value;
  const selectedRoute = document.getElementById("routeFilter").value.toLowerCase();

  const filtered = uniqueDoctors.filter((item) => {
    const name = (item.doctorName || "").toLowerCase();
    const specialization = (item.specialization || "").toLowerCase();
    const hospital = (item.hospitalName || "").toLowerCase();
    const landmark = (item.landmark || "").toLowerCase();
    const mrName = item.employeeName || "";
    const route = (item.routeName || "").toLowerCase();

    const matchesSearch = name.includes(keyword) || specialization.includes(keyword) || hospital.includes(keyword) || landmark.includes(keyword);
    const matchesMr = selectedMr === "" || mrName === selectedMr;
    const matchesRoute = selectedRoute === "" || route === selectedRoute;

    return matchesSearch && matchesMr && matchesRoute;
  });

  currentDisplayLimit = 50; // Reset limit on new search
  displayDoctors(filtered);
}

// 🚀 FIXED: Pagination to stop browser hanging
function displayDoctors(list) {
  const container = document.getElementById("doctorList");
  container.innerHTML = "";
  currentDoctorList = list;

  if (!list || list.length === 0) {
    const categoryText = selectedCategory === "CHEMIST" ? "chemists" : "doctors";
    container.innerHTML = `<div class="col-12"><div class="alert alert-warning text-center">No ${categoryText} found</div></div>`;
    return;
  }

  // Sirf 50 (ya current limit) tak hi render karenge
  const listToRender = list.slice(0, currentDisplayLimit);
  let html = "";

  listToRender.forEach((doctor, index) => {
    const isChemist = (doctor.visitCategory || selectedCategory) === "CHEMIST";
    const iconClass = isChemist ? "fa-prescription-bottle-medical" : "fa-user-doctor";
    const secondaryText = isChemist ? doctor.hospitalName || "-" : doctor.specialization || "-";

    html += `
      <div class="col-md-6 col-lg-4">
        <div class="card doctor-card h-100" onclick="openDoctorFromList(${index})">
          <div class="card-body">
            
            <div class="d-flex align-items-center gap-3 mb-3">
              <div class="icon-box">
                <i class="fa-solid ${iconClass}"></i>
              </div>
              <div>
                <h5 class="fw-bold mb-1">${doctor.doctorName || "-"}</h5>
                <p class="text-muted mb-0" style="font-size:13px;">${secondaryText}</p>
              </div>
            </div>

            <p class="mb-1" style="font-size:13px;">
              <i class="fa-solid fa-user-tie me-2 text-primary"></i> <b>MR:</b> ${doctor.employeeName || "-"}
            </p>
            <p class="mb-1" style="font-size:13px;">
              <i class="fa-solid fa-route me-2 text-warning"></i> <b>Route:</b> ${doctor.routeName || "-"}
            </p>
            <p class="mb-1" style="font-size:13px;">
              <i class="fa-solid fa-location-dot me-2 text-danger"></i> ${doctor.landmark || "-"}
            </p>
            <p class="mb-0" style="font-size:13px;">
              <i class="fa-solid fa-calendar me-2 text-success"></i> <b>Last Visit:</b> ${doctor.visitDate || "-"}
            </p>

          </div>
        </div>
      </div>
    `;
  });

  // Agar total records 50 se zyada hain, toh Load More ka button lagayenge
  if (list.length > currentDisplayLimit) {
    html += `
      <div class="col-12 text-center mt-3">
        <button class="btn btn-outline-primary px-4 py-2" onclick="loadMoreDoctors()" style="border-radius:10px; font-weight:600;">
          Load More (Showing ${currentDisplayLimit} of ${list.length})
        </button>
      </div>
    `;
  }

  container.innerHTML = html;
}

// Load More function
function loadMoreDoctors() {
  currentDisplayLimit += 50; 
  displayDoctors(currentDoctorList); // Wapas display call karo with new limit
}

function openDoctorFromList(index) {
  const doctor = currentDoctorList[index];
  if (!doctor) return alert("Doctor details not found");
  
  const isChemist = (doctor.visitCategory || selectedCategory) === "CHEMIST";

  document.getElementById("modalSpecializationLabel").innerText = isChemist ? "Type" : "Specialization";
  document.getElementById("modalHospitalLabel").innerText = isChemist ? "Chemist Shop" : "Hospital / Clinic";
  
  document.getElementById("modalDoctorName").innerText = doctor.doctorName || "-";
  document.getElementById("modalMrName").innerText = doctor.employeeName || "-";
  document.getElementById("modalRoute").innerText = doctor.routeName || "-";
  document.getElementById("modalSpecialization").innerText = isChemist ? "-" : doctor.specialization || "-";
  document.getElementById("modalHospital").innerText = doctor.hospitalName || "-";
  document.getElementById("modalArea").innerText = doctor.landmark || "-";
  document.getElementById("modalLocation").innerText = doctor.location || "-";
  document.getElementById("modalDob").innerText = doctor.dob || "-";
  document.getElementById("modalAnniversary").innerText = doctor.anniversaryDate || "-";
  document.getElementById("modalVisitDate").innerText = doctor.visitDate || "-";

  const mobile = doctor.mobileNumber || doctor.mobile || doctor.phone;
  const callBtn = document.getElementById("callNowBtn");
  
  if (mobile && mobile !== "N/A" && mobile !== "-") {
      callBtn.style.display = "block";
      callBtn.href = `tel:${mobile}`;
  } else {
      callBtn.style.display = "none";
  }

  const modal = new bootstrap.Modal(document.getElementById("doctorModal"));
  modal.show();
}