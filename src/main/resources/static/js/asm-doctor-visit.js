// ==========================================
// ASM DOCTOR VISIT JAVASCRIPT
// ==========================================

const employeeId = localStorage.getItem("asmId") || localStorage.getItem("employeeId");
const employeeName = localStorage.getItem("asmName") || localStorage.getItem("employeeName");

let stream = null;
let imageBase64 = null;
let currentFacingMode = "environment"; // Default back camera

window.onload = function () {
  if (typeof checkAsmSession === "function") {
    checkAsmSession();
  }
  
  // Initialize page functions
  getLocation();
  loadAsmRoutes();
  loadVisitHistory();
};

// 1. Visit Category Handle (Doctor/Chemist)
function handleVisitCategoryChange() {
  const category = document.querySelector('input[name="visitCategory"]:checked')?.value;
  const label = document.getElementById("nameFieldLabel");
  const input = document.getElementById("doctorName");
  const specDiv = document.getElementById("specializationDiv");
  const hospLabel = document.getElementById("hospitalLabel");

  if (category === "CHEMIST") {
    label.innerText = "Chemist Name";
    input.placeholder = "Enter Chemist Name";
    specDiv.style.display = "none";
    hospLabel.innerText = "Chemist Shop Name";
    document.getElementById("specialization").value = "N/A";
  } else {
    label.innerText = "Doctor Name";
    input.placeholder = "Enter Doctor Name";
    specDiv.style.display = "block";
    hospLabel.innerText = "Hospital / Clinic Name";
    document.getElementById("specialization").value = "";
  }
}

// 2. Load Assigned Routes for ASM
function loadAsmRoutes() {
  fetch(`${BASE_URL}/asm/routes/${employeeId}`)
    .then((res) => res.json())
    .then((data) => {
      const routeSelect = document.getElementById("routeName");
      routeSelect.innerHTML = '<option value="">Select Route</option>';
      data.forEach((r) => {
        routeSelect.innerHTML += `<option value="${r.routeName}">${r.routeName} (${r.headquarterName})</option>`;
      });
    })
    .catch((err) => console.error("Error loading ASM routes:", err));
}

// 3. Fetch GPS Location (Updated with MR OpenStreetMap Logic)
function getLocation() {
  const locInput = document.getElementById("locationName");
  if (navigator.geolocation) {
    locInput.value = "Fetching GPS Location...";
    navigator.geolocation.getCurrentPosition(
      (position) => {
        const lat = position.coords.latitude;
        const lng = position.coords.longitude;
        const acc = position.coords.accuracy;

        document.getElementById("latitude").value = lat;
        document.getElementById("longitude").value = lng;
        document.getElementById("accuracy").value = acc;

        // Fetching Real Address Text from Coordinates
        fetch(`https://nominatim.openstreetmap.org/reverse?format=json&addressdetails=1&zoom=18&lat=${lat}&lon=${lng}`)
          .then((response) => response.json())
          .then((data) => {
            locInput.value = data.display_name || "Location found";
          })
          .catch((error) => {
            console.error(error);
            locInput.value = `${lat.toFixed(6)}, ${lng.toFixed(6)}`;
          });
      },
      (error) => {
        console.error(error);
        locInput.value = "Location access denied";
        alert("Please enable GPS/Location to submit visits.");
      },
      { enableHighAccuracy: true, timeout: 30000, maximumAge: 0 }
    );
  } else {
    locInput.value = "Geolocation not supported";
  }
}

// 4. Camera Handling
function openCamera() {
  document.getElementById("cameraPreview").classList.remove("d-none");
  document.getElementById("captureBtn").classList.remove("d-none");
  document.getElementById("switchCameraBtn").classList.remove("d-none");
  startCamera();
}
function startCamera() {
  if (stream) {
    stream.getTracks().forEach((track) => track.stop());
  }
  navigator.mediaDevices
    .getUserMedia({ video: { facingMode: currentFacingMode } })
    .then((s) => {
      stream = s;
      document.getElementById("cameraPreview").srcObject = stream;
    })
    .catch((err) => {
      console.error("Camera error:", err);
      alert("Could not access camera. Please check permissions.");
    });
}
function switchCamera() {
  currentFacingMode = currentFacingMode === "environment" ? "user" : "environment";
  startCamera();
}
function capturePhoto() {
  const video = document.getElementById("cameraPreview");
  const canvas = document.getElementById("cameraCanvas");
  canvas.width = video.videoWidth;
  canvas.height = video.videoHeight;
  canvas.getContext("2d").drawImage(video, 0, 0);
  
  imageBase64 = canvas.toDataURL("image/jpeg", 0.7);
  
  document.getElementById("capturedImage").src = imageBase64;
  document.getElementById("capturedImage").classList.remove("d-none");
  video.classList.add("d-none");
  
  document.getElementById("captureBtn").classList.add("d-none");
  document.getElementById("switchCameraBtn").classList.add("d-none");
  document.getElementById("retakeBtn").classList.remove("d-none");
  if (stream) {
    stream.getTracks().forEach((track) => track.stop());
  }
}
function retakePhoto() {
  document.getElementById("capturedImage").classList.add("d-none");
  document.getElementById("retakeBtn").classList.add("d-none");
  imageBase64 = null;
  openCamera();
}

// 5. Submit Doctor Visit
function submitDoctorVisit() {
  const btn = document.getElementById("submitVisitBtn");
  const category = document.querySelector('input[name="visitCategory"]:checked')?.value;
  const doctorName = document.getElementById("doctorName").value;
  const routeName = document.getElementById("routeName").value;
  const latitude = document.getElementById("latitude").value;
  
  if (!category) return alert("Please select visit category (Doctor or Chemist)");
  if (!doctorName) return alert("Please enter Doctor/Chemist name");
  if (!routeName) return alert("Please select a route");
  if (!latitude) return alert("Please wait for GPS location to load");
  if (!imageBase64) return alert("Please capture an image to proceed");
  
  btn.disabled = true;
  btn.innerHTML = `<i class="fa-solid fa-spinner fa-spin me-2"></i>Submitting...`;
  
  const now = new Date();
  const pad = (n) => (n < 10 ? "0" + n : n);
  const visitDate = `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())}`;
  const visitTime = `${pad(now.getHours())}:${pad(now.getMinutes())}:${pad(now.getSeconds())}`;
  
  const payload = {
    employeeId: employeeId,
    employeeName: employeeName,
    workingWith: document.getElementById("workingWith").value,
    workingPersonName: document.getElementById("workingPersonName").value,
    visitCategory: category,
    doctorName: doctorName,
    specialization: document.getElementById("specialization").value,
    dob: document.getElementById("dob").value,
    anniversaryDate: document.getElementById("anniversaryDate").value,
    hospitalName: document.getElementById("hospitalName").value,
    mobileNumber: document.getElementById("mobileNumber").value,
    location: document.getElementById("locationName").value, // Database me text address jayega
    latitude: latitude,
    longitude: document.getElementById("longitude").value,
    routeName: routeName,
    landmark: document.getElementById("landmark").value,
    visitImage: imageBase64,
    remarks: document.getElementById("remarks").value,
    visitDate: visitDate,
    visitTime: visitTime,
    status: "Pending"
  };

  fetch(`${BASE_URL}/doctor-visit/save`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload)
  })
    .then((res) => {
      if (!res.ok) throw new Error("Failed to save visit");
      return res.json();
    })
    .then((data) => {
      alert("Visit submitted successfully!");
      window.location.reload();
    })
    .catch((err) => {
      console.error(err);
      alert("Error submitting visit. Please try again.");
      btn.disabled = false;
      btn.innerHTML = `<i class="fa-solid fa-paper-plane me-2"></i>Submit Visit`;
    });
}

// 6. Load Visit History (Map Link Hata Diya)
function loadVisitHistory() {
  fetch(`${BASE_URL}/asm/mr-visits/${employeeId}`)
    .then((res) => res.json())
    .then((data) => {
      const table = document.getElementById("doctorVisitTable");
      table.innerHTML = "";
      
      if (!data || data.length === 0) {
        table.innerHTML = `<tr><td colspan="18" class="text-center text-muted py-3">No visits found</td></tr>`;
        return;
      }
      
      data.forEach((visit) => {
        let statusClass = "status-pending";
        if (visit.status === "Approved" || visit.status === "Completed") statusClass = "status-completed";
        if (visit.status === "Rejected") statusClass = "status-rejected";

        let imgBtn = visit.visitImage
          ? `<button class="btn btn-sm btn-outline-primary" onclick="viewImage('${visit.visitImage}')"><i class="fa-solid fa-image"></i> View</button>`
          : `-`;

        table.innerHTML += `
          <tr>
            <td>${visit.workingWith || "-"}</td>
            <td>${visit.workingPersonName || "-"}</td>
            <td><span class="badge bg-secondary">${visit.visitCategory || "DOCTOR"}</span></td>
            <td><strong>${visit.doctorName || "-"}</strong></td>
            <td>${visit.specialization || "-"}</td>
            <td>${visit.mobileNumber || "-"}</td>
            <td>${visit.dob || "-"}</td>
            <td>${visit.anniversaryDate || "-"}</td>
            <td>${visit.hospitalName || "-"}</td>
            <td>${visit.visitDate || "-"}</td>
            <td>${visit.visitTime || "-"}</td>
            
            <!-- Map link removed. Ab sirf Database me save hua proper Address Text dikhega -->
            <td style="max-width: 250px; white-space: normal; font-size: 13px;">
                ${visit.location || "-"}
            </td>
            
            <td>${visit.landmark || "-"}</td>
            <td>Good</td>
            <td>${imgBtn}</td>
            <td>${visit.remarks || "-"}</td>
            <td class="${statusClass}">${visit.status || "Pending"}</td>
            <td>
              <button class="btn btn-sm btn-outline-primary" onclick='openEditModal(${JSON.stringify(visit).replace(/'/g, "&#39;")})'>
                 <i class="fa-solid fa-pen"></i>
              </button>
            </td>
          </tr>
        `;
      });
    })
    .catch((err) => console.error("History fetch error:", err));
}

// 7. View Image Modal
function viewImage(base64Str) {
  document.getElementById("visitFullImage").src = base64Str;
  const modal = new bootstrap.Modal(document.getElementById("visitImageModal"));
  modal.show();
}

// 8. Open Edit Modal
function openEditModal(visit) {
  document.getElementById("editVisitId").value = visit.id;
  document.getElementById("editWorkingWith").value = visit.workingWith || "Individual";
  document.getElementById("editWorkingPersonName").value = visit.workingPersonName || "";
  document.getElementById("editDob").value = visit.dob || "";
  document.getElementById("editAnniversaryDate").value = visit.anniversaryDate || "";
  document.getElementById("editMobileNumber").value = visit.mobileNumber || "";
  document.getElementById("editLandmark").value = visit.landmark || "";
  document.getElementById("editRemarks").value = visit.remarks || "";

  const modal = new bootstrap.Modal(document.getElementById("editVisitModal"));
  modal.show();
}

// 9. Update Doctor Visit
function updateDoctorVisit() {
  const id = document.getElementById("editVisitId").value;
  const payload = {
    workingWith: document.getElementById("editWorkingWith").value,
    workingPersonName: document.getElementById("editWorkingPersonName").value,
    dob: document.getElementById("editDob").value,
    anniversaryDate: document.getElementById("editAnniversaryDate").value,
    mobileNumber: document.getElementById("editMobileNumber").value,
    landmark: document.getElementById("editLandmark").value,
    remarks: document.getElementById("editRemarks").value,
  };

  fetch(`${BASE_URL}/doctor-visit/update/${id}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  })
    .then((res) => {
        if(!res.ok) throw new Error("Update Failed");
        return res.json();
    })
    .then((data) => {
      alert("Visit updated successfully!");
      window.location.reload();
    })
    .catch((err) => {
      console.error(err);
      alert("Error updating visit.");
    });
}

// Empty search method to prevent console error from UI oninput
function searchDoctor() {
   // Kept empty
}