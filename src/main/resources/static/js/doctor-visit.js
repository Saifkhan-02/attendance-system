
const asmId = localStorage.getItem("asmId") || localStorage.getItem("employeeId");
const asmName = localStorage.getItem("asmName") || localStorage.getItem("employeeName");

let cameraStream = null;
let capturedPhotoBase64 = "";
let capturedVisitBlob = null;
let isSubmittingVisit = false;
let currentFacingMode = "user";

window.onload = function () {
  if (typeof checkAsmSession === "function") {
    checkAsmSession();
  }
  loadDoctorVisitHistory();
  getCurrentLocation();
  loadAssignedRoutes(); // ASM specific routes
};

// 1. Load ASM Routes
async function loadAssignedRoutes() {
  const routeDropdown = document.getElementById("routeName");
  routeDropdown.innerHTML = '<option value="">Select Route</option>';

  try {
    // ASM Specific Route API
    const response = await fetch(`${BASE_URL}/asm/routes/${asmId}`);
    if (!response.ok) throw new Error("Failed to load routes");

    const routes = await response.json();
    routes.forEach((route) => {
      routeDropdown.innerHTML += `<option value="${route.routeName}">${route.routeName} (${route.headquarterName})</option>`;
    });
  } catch (e) {
    console.error(e);
  }
}

function getSelectedVisitCategory() {
  const selected = document.querySelector('input[name="visitCategory"]:checked');
  return selected ? selected.value : "";
}

// 2. Handle Category Change
function handleVisitCategoryChange() {
  const category = getSelectedVisitCategory();
  const nameLabel = document.getElementById("nameFieldLabel");
  const nameInput = document.getElementById("doctorName");
  const specializationDiv = document.getElementById("specializationDiv");
  const specializationLabel = document.getElementById("specializationLabel");
  const specializationInput = document.getElementById("specialization");
  const hospitalLabel = document.getElementById("hospitalLabel");
  const hospitalInput = document.getElementById("hospitalName");

  if (!nameLabel || !nameInput || !specializationLabel || !specializationInput || !hospitalLabel || !hospitalInput) {
    return;
  }

  if (category === "DOCTOR") {
    nameLabel.innerText = "Doctor Name";
    nameInput.placeholder = "Enter doctor name";
    specializationDiv.style.display = "";
    hospitalLabel.innerText = "Hospital / Clinic";
    hospitalInput.placeholder = "Enter hospital or clinic";
  } else if (category === "CHEMIST") {
    nameLabel.innerText = "Chemist Name";
    nameInput.placeholder = "Enter chemist name";
    specializationDiv.style.display = "none";
    hospitalLabel.innerText = "Chemist Shop Name";
    hospitalInput.placeholder = "Enter chemist shop name";
  }

  clearPartyFields();
}

function clearPartyFields() {
  document.getElementById("doctorName").value = "";
  document.getElementById("specialization").value = "";
  document.getElementById("dob").value = "";
  document.getElementById("anniversaryDate").value = "";
  document.getElementById("hospitalName").value = "";
  document.getElementById("landmark").value = "";

  document.getElementById("doctorStatus").innerHTML = "";
  document.getElementById("doctorSuggestionBox").innerHTML = "";
  document.getElementById("doctorSuggestionBox").style.display = "none";
  document.getElementById("doctorInfoCard").style.display = "none";
}

// 3. Search Existing Party
async function searchDoctor() {
  const visitCategory = getSelectedVisitCategory();
  if (!visitCategory) {
    alert("Please select Doctor or Chemist first");
    document.getElementById("doctorName").value = "";
    return;
  }

  const keyword = document.getElementById("doctorName").value.trim();
  document.getElementById("doctorStatus").innerHTML = "";
  document.getElementById("doctorInfoCard").style.display = "none";
  const suggestionBox = document.getElementById("doctorSuggestionBox");

  if (keyword.length < 2) {
    suggestionBox.style.display = "none";
    suggestionBox.innerHTML = "";
    return;
  }

  try {
    const response = await fetch(
      `${BASE_URL}/doctor-visit/search-party?employeeId=${asmId}&visitCategory=${encodeURIComponent(visitCategory)}&name=${encodeURIComponent(keyword)}`
    );
    const doctors = await response.json();
    document.getElementById("doctorStatus").innerHTML = "";
    suggestionBox.innerHTML = "";

    if (doctors.length === 0) {
      const categoryText = visitCategory === "CHEMIST" ? "Chemist" : "Doctor";
      document.getElementById("doctorStatus").innerHTML = `<div class="alert alert-warning py-2 mb-0"><i class="fa-solid fa-circle-plus"></i> New ${categoryText}</div>`;
      suggestionBox.style.display = "none";
      suggestionBox.innerHTML = "";
      return;
    }

    suggestionBox.style.display = "block";
    doctors.forEach((doctor) => {
      const isChemist = visitCategory === "CHEMIST";
      const iconClass = isChemist ? "fa-prescription-bottle-medical" : "fa-user-doctor";
      const secondaryLabel = isChemist ? "Shop" : "Hospital";

      suggestionBox.innerHTML += `
        <div class="list-group-item list-group-item-action" style="cursor:pointer" onclick='checkDuplicateDoctor("${doctor.doctorName}")'>
          <div class="fw-bold text-primary"><i class="fa-solid ${iconClass}"></i> ${doctor.doctorName}</div>
          <div class="small text-dark mt-1"><i class="fa-solid fa-building"></i> ${secondaryLabel}: ${doctor.hospitalName || "-"}</div>
          <div class="small text-secondary">${doctor.specialization || "-"}</div>
          <div class="small text-success"><i class="fa-solid fa-location-dot"></i> ${doctor.landmark || "-"}</div>
        </div>
      `;
    });
  } catch (e) {
    console.error(e);
  }
}

function selectDoctor(doctor) {
  document.getElementById("doctorName").value = doctor.doctorName || "";
  document.getElementById("specialization").value = doctor.specialization || "";
  document.getElementById("dob").value = doctor.dob || "";
  document.getElementById("anniversaryDate").value = doctor.anniversaryDate || "";
  document.getElementById("hospitalName").value = doctor.hospitalName || "";
  document.getElementById("mobileNumber").value = doctor.mobileNumber || "";
  document.getElementById("landmark").value = doctor.landmark || "";

  document.getElementById("doctorStatus").innerHTML = `<div class="alert alert-success py-2 mb-0"><i class="fa-solid fa-circle-check"></i> Existing Doctor</div>`;
  document.getElementById("doctorInfoCard").style.display = "block";
  document.getElementById("lastVisitDate").innerText = doctor.visitDate || "-";
  document.getElementById("totalVisitCount").innerText = doctor.totalVisitCount || 0;

  document.getElementById("doctorSuggestionBox").style.display = "none";
  document.getElementById("doctorSuggestionBox").innerHTML = "";
}

async function checkDuplicateDoctor(doctorName) {
  const visitCategory = getSelectedVisitCategory();
  const response = await fetch(`${BASE_URL}/doctor-visit/exact-party?employeeId=${asmId}&visitCategory=${encodeURIComponent(visitCategory)}&name=${encodeURIComponent(doctorName)}`);

  if (!response.ok) {
    console.error(await response.text());
    return;
  }

  const doctors = await response.json();
  if (doctors.length <= 1) {
    selectDoctor(doctors[0]);
    return;
  }

  let html = "";
  doctors.forEach((doctor) => {
    html += `
      <div class="card mb-2">
        <div class="card-body">
          <h6>${doctor.doctorName}</h6>
          <p class="mb-1"><b>Hospital :</b> ${doctor.hospitalName}</p>
          <p class="mb-1"><b>Specialization :</b> ${doctor.specialization}</p>
          <p class="mb-2"><b>Landmark :</b> ${doctor.landmark}</p>
          <button class="btn btn-primary btn-sm" onclick='selectDoctor(${JSON.stringify(doctor).replace(/'/g, "&#39;")}); bootstrap.Modal.getInstance(document.getElementById("doctorSelectModal")).hide();'>Select</button>
        </div>
      </div>
    `;
  });

  document.getElementById("doctorListContainer").innerHTML = html;
  new bootstrap.Modal(document.getElementById("doctorSelectModal")).show();
}

// 4. Camera Setup
function openCamera() {
  if (cameraStream) {
    cameraStream.getTracks().forEach(track => track.stop());
  }
  navigator.mediaDevices.getUserMedia({ video: { facingMode: currentFacingMode }, audio: false })
    .then(function(stream) {
      cameraStream = stream;
      const video = document.getElementById("cameraPreview");
      video.srcObject = stream;
      video.classList.remove("d-none");
      document.getElementById("captureBtn").classList.remove("d-none");
      document.getElementById("capturedImage").classList.add("d-none");
      document.getElementById("retakeBtn").classList.add("d-none");
      document.getElementById("switchCameraBtn").classList.remove("d-none");
    })
    .catch(function(error) {
      console.error(error);
      alert("Unable to open camera.");
    });
}

function switchCamera() {
  currentFacingMode = currentFacingMode === "user" ? "environment" : "user";
  openCamera();
}

function capturePhoto() {
  const video = document.getElementById("cameraPreview");
  const canvas = document.getElementById("cameraCanvas");
  const img = document.getElementById("capturedImage");

  if (!video.videoWidth || !video.videoHeight) {
    alert("Camera is not ready. Please wait 2 seconds and try to capture again.");
    return;
  }

  canvas.width = video.videoWidth;
  canvas.height = video.videoHeight;
  const ctx = canvas.getContext("2d");
  ctx.drawImage(video, 0, 0, canvas.width, canvas.height);

  capturedPhotoBase64 = canvas.toDataURL("image/jpeg", 0.8);
  canvas.toBlob(function (blob) {
    capturedVisitBlob = blob;
  }, "image/jpeg", 0.7);

  img.src = capturedPhotoBase64;
  img.classList.remove("d-none");
  video.classList.add("d-none");
  document.getElementById("captureBtn").classList.add("d-none");
  document.getElementById("switchCameraBtn").classList.add("d-none");
  document.getElementById("retakeBtn").classList.remove("d-none");

  if (cameraStream) {
    cameraStream.getTracks().forEach((track) => track.stop());
  }
}

function retakePhoto() {
  capturedPhotoBase64 = "";
  document.getElementById("capturedImage").src = "";
  document.getElementById("capturedImage").classList.add("d-none");
  document.getElementById("retakeBtn").classList.add("d-none");
  openCamera();
}

// 5. GPS Location
function getCurrentLocation() {
  const locationInput = document.getElementById("locationName");
  if (!navigator.geolocation) {
    locationInput.value = "Location not supported";
    return;
  }

  locationInput.value = "Fetching location...";
  navigator.geolocation.getCurrentPosition(
    function (position) {
      const lat = position.coords.latitude;
      const lng = position.coords.longitude;
      const acc = position.coords.accuracy;

      document.getElementById("latitude").value = lat;
      document.getElementById("longitude").value = lng;
      document.getElementById("accuracy").value = acc;

      fetch(`https://nominatim.openstreetmap.org/reverse?format=json&addressdetails=1&zoom=18&lat=${lat}&lon=${lng}`)
        .then((response) => response.json())
        .then((data) => {
          locationInput.value = data.display_name || "Location found";
        })
        .catch((error) => {
          console.error(error);
          locationInput.value = "Location found";
        });
    },
    function (error) {
      console.error(error);
      locationInput.value = "";
      alert("Please go to your settings and allow Location permission");
    },
    { enableHighAccuracy: true, timeout: 30000, maximumAge: 0 }
  );
}

// 6. Submit Doctor Visit
async function uploadDoctorVisitPhoto() {
  if (!capturedVisitBlob) {
    alert("Please capture image first");
    return null;
  }

  const formData = new FormData();
  formData.append("file", capturedVisitBlob, "doctor-visit.jpg");

  const response = await fetch(`${BASE_URL}/doctor-visit/upload-photo/${asmId}`, {
    method: "POST",
    body: formData,
  });

  if (!response.ok) {
    const errorText = await response.text();
    console.error("PHOTO UPLOAD ERROR:", errorText);
    alert("Photo upload failed");
    return null;
  }
  return await response.text();
}

async function submitDoctorVisit() {
  const visitCategory = getSelectedVisitCategory();
  if (!visitCategory) return alert("Please select Doctor or Chemist");

  const workingWith = document.getElementById("workingWith").value;
  if (!workingWith) return alert("Please select Working With");
  if (workingWith != "Individual" && !document.getElementById("workingPersonName").value.trim()) {
    return alert("Please enter person Name");
  }

  if (!document.getElementById("doctorName").value.trim()) {
    return alert(visitCategory === "CHEMIST" ? "Chemist name required" : "Doctor name required");
  }
  if (visitCategory === "DOCTOR" && !document.getElementById("specialization").value.trim()) {
    return alert("Please fill Specialization First");
  }
  if (!document.getElementById("hospitalName").value.trim()) return alert("Please fill Hospital / Clinic First");
  if (!document.getElementById("locationName").value.trim()) return alert("Your Current location is not accurate");
  if (!document.getElementById("routeName").value) return alert("Please select Route");
  if (!document.getElementById("landmark").value.trim()) return alert("Please enter Landmark / Clinic Area");
  if (!capturedVisitBlob) return alert("Please capture image first");

  if (isSubmittingVisit) return;

  const submitBtn = document.getElementById("submitVisitBtn");
  isSubmittingVisit = true;
  submitBtn.disabled = true;
  submitBtn.innerHTML = "Submitting...";

  const photoUrl = await uploadDoctorVisitPhoto();
  if (!photoUrl) {
    isSubmittingVisit = false;
    submitBtn.disabled = false;
    submitBtn.innerHTML = `<i class="fa-solid fa-paper-plane me-2"></i> Submit Visit`;
    return;
  }

  const visitData = {
    employeeId: asmId,
    employeeName: asmName,
    visitCategory: visitCategory,
    workingWith: workingWith,
    workingPersonName: document.getElementById("workingPersonName").value,
    doctorName: document.getElementById("doctorName").value,
    specialization: visitCategory === "DOCTOR" ? document.getElementById("specialization").value.trim() : "",
    dob: document.getElementById("dob").value,
    anniversaryDate: document.getElementById("anniversaryDate").value,
    hospitalName: document.getElementById("hospitalName").value,
    mobileNumber: document.getElementById("mobileNumber").value.trim(),
    location: document.getElementById("locationName").value,
    landmark: document.getElementById("landmark").value,
    routeName: document.getElementById("routeName").value,
    latitude: document.getElementById("latitude").value,
    longitude: document.getElementById("longitude").value,
    accuracy: document.getElementById("accuracy").value,
    remarks: document.getElementById("remarks").value,
    photo: photoUrl,
  };

  fetch(`${BASE_URL}/doctor-visit/save`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(visitData),
  })
    .then(async (response) => {
      const text = await response.text();
      if (!response.ok) throw new Error(text);

      alert("Doctor Visit Submitted Successfully");
      clearForm();
      loadDoctorVisitHistory();
      isSubmittingVisit = false;
      submitBtn.disabled = false;
      submitBtn.innerHTML = `<i class="fa-solid fa-paper-plane me-2"></i> Submit Visit`;
    })
    .catch((error) => {
      isSubmittingVisit = false;
      submitBtn.disabled = false;
      submitBtn.innerHTML = `<i class="fa-solid fa-paper-plane me-2"></i> Submit Visit`;
      console.error("SAVE ERROR:", error);
      alert(error.message || "Failed To Submit Visit");
    });
}

function clearForm() {
  document.querySelectorAll('input[name="visitCategory"]').forEach((input) => input.checked = false);
  document.getElementById("nameFieldLabel").innerText = "Doctor / Chemist Name";
  document.getElementById("doctorName").placeholder = "First select Doctor or Chemist";
  document.getElementById("workingWith").selectedIndex = 0;
  document.getElementById("workingPersonName").value = "";
  document.getElementById("doctorName").value = "";
  document.getElementById("mobileNumber").value = "";
  document.getElementById("specialization").value = "";
  document.getElementById("dob").value = "";
  document.getElementById("anniversaryDate").value = "";
  document.getElementById("hospitalName").value = "";
  document.getElementById("landmark").value = "";
  document.getElementById("remarks").value = "";

  capturedPhotoBase64 = "";
  capturedVisitBlob = null;
  document.getElementById("capturedImage").src = "";
  document.getElementById("capturedImage").classList.add("d-none");
  document.getElementById("cameraPreview").classList.add("d-none");
  document.getElementById("captureBtn").classList.add("d-none");
  document.getElementById("retakeBtn").classList.add("d-none");
  
  getCurrentLocation();
}

// 7. Load Visit History (ASM Specific)
function loadDoctorVisitHistory() {
  fetch(`${BASE_URL}/asm/mr-visits/${asmId}`)
    .then((response) => response.json())
    .then((data) => {
      const table = document.getElementById("doctorVisitTable");
      table.innerHTML = "";

      if (data.length === 0) {
        table.innerHTML = `<tr><td colspan="18" class="text-center">No Doctor Visits Found</td></tr>`;
        return;
      }

      data.forEach((visit) => {
        let statusClass = "status-completed";
        if (visit.status === "Rejected") statusClass = "status-rejected";
        if (visit.status === "Pending") statusClass = "status-pending";

        table.innerHTML += `
          <tr>
            <td>${visit.workingWith || "-"}</td>
            <td>${visit.workingPersonName || "-"}</td>
            <td><span class="badge ${visit.visitCategory === "CHEMIST" ? "bg-success" : "bg-primary"}">${visit.visitCategory || "DOCTOR"}</span></td>
            <td>${visit.doctorName || "-"}</td>
            <td>${visit.specialization || "-"}</td>
            <td>${visit.mobileNumber || "-"}</td>
            <td>${visit.dob || "-"}</td>
            <td>${visit.anniversaryDate || "-"}</td>
            <td>${visit.hospitalName || "-"}</td>
            <td>${visit.visitDate || "-"}</td>
            <td>${visit.visitTime || "-"}</td>
            <td>${visit.location || "-"}</td>
            <td>${visit.landmark || "-"}</td>
            <td>${visit.accuracy ? visit.accuracy + " m" : "-"}</td>
            <td><img src="${visit.photo}" onclick="openVisitImage('${visit.photo}')" style="width:55px;height:55px;object-fit:cover;border-radius:10px;cursor:pointer;"></td>
            <td>${visit.remarks || "-"}</td>
            <td><span class="${statusClass}">${visit.status || "Completed"}</span></td>
            <td>
                <button class="btn btn-sm btn-primary" onclick='openEditVisitModal(${JSON.stringify(visit).replace(/'/g, "&#39;")})'>
                  <i class="fa-solid fa-pen"></i>
                </button>
            </td>
          </tr>
        `;
      });
    })
    .catch((error) => console.error(error));
}

// 8. Edit / Modals
function openVisitImage(imageUrl) {
  document.getElementById("visitFullImage").src = imageUrl;
  new bootstrap.Modal(document.getElementById("visitImageModal")).show();
}

function openEditVisitModal(visit) {
  document.getElementById("editVisitId").value = visit.id;
  document.getElementById("editWorkingWith").value = visit.workingWith || "Individual";
  document.getElementById("editWorkingPersonName").value = visit.workingPersonName || "";
  document.getElementById("editDob").value = visit.dob || "";
  document.getElementById("editAnniversaryDate").value = visit.anniversaryDate || "";
  document.getElementById("editMobileNumber").value = visit.mobileNumber || "";
  document.getElementById("editLandmark").value = visit.landmark || "";
  document.getElementById("editRemarks").value = visit.remarks || "";

  new bootstrap.Modal(document.getElementById("editVisitModal")).show();
}

function updateDoctorVisit() {
  const visitId = document.getElementById("editVisitId").value;
  const updatedVisit = {
    workingWith: document.getElementById("editWorkingWith").value,
    workingPersonName: document.getElementById("editWorkingPersonName").value,
    dob: document.getElementById("editDob").value,
    anniversaryDate: document.getElementById("editAnniversaryDate").value,
    mobileNumber: document.getElementById("editMobileNumber").value,
    landmark: document.getElementById("editLandmark").value,
    remarks: document.getElementById("editRemarks").value,
  };

  fetch(`${BASE_URL}/doctor-visit/update/${visitId}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(updatedVisit),
  })
    .then((response) => {
      if (!response.ok) throw new Error("Update failed");
      return response.json();
    })
    .then((data) => {
      document.activeElement.blur();
      bootstrap.Modal.getInstance(document.getElementById("editVisitModal")).hide();
      alert("Visit updated successfully");
      loadDoctorVisitHistory();
    })
    .catch((error) => {
      console.error(error);
      alert("Failed to update visit");
    });
}