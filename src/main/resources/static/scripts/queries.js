const HOST = "http://localhost:8080/"
const userIdStorage = localStorage.getItem('userId');
if(userIdStorage != null) fetchUserProfile(userIdStorage)

async function getUserById(id, table) {
  try {
      const response = await fetch(HOST + table + "/" + id)
      const data = await response.json();
      return data;
  } catch (error) {
      console.error('An error occurred while receiving user data:', error)
  }
}

async function getAllUsers() {
  try {
      let response = await fetch(HOST + "webUsers/all");
      let data = await response.json();
      const tableBody = document.querySelector('#category-table tbody');
      
      data.forEach(user => {
          const row = document.createElement('tr');
          row.innerHTML = `
              <td>${user[0]}</td>
              <td>${user[1]}</td>
              <td>${user[2]}</td>
              <td>${user[3]}</td>
              <td>${user[4]}</td>
              <td>
                  <button onclick='approveRegistration(${user[0]}, "${user[3]}", "${user[5]}")'><i class="fa-solid fa-check"></i></button>
                  <button onclick='editUser(${user[0]}, "${user[5]}", "${user[5]}")'><i class="fa-regular fa-pen-to-square"></i></button>
                  <button onclick='deleteUser(${user[0]}, "${user[5]}")'><i class="fa-regular fa-trash-can"></i></button>
              </td>
          `;
          tableBody.appendChild(row);
          
      });

  } catch (error) {
      console.error('An error occurred while receiving all users data:', error);
  }
}

getAllUsers()

function editUser(userId, tableName) {
  const editUserPageUrl = `editUser.html?userId=${userId}&table=${tableName}`;
  window.location.href = editUserPageUrl;
}


async function displayAdminUsers() {
    try {
        let response = await fetch(HOST + "webUsers/all");
        let data = await response.json();
        const tableBody = document.querySelector('#admins-table tbody');
        
        tableBody.innerHTML = "";

        data
            .filter(user => user[3] === 'admin')
            .forEach(adminUser => {
                const row = document.createElement('tr');
                row.innerHTML = `
                    <td>${adminUser[0]}</td>
                    <td>${adminUser[1]}</td>
                    <td>${adminUser[2]}</td>
                    <td>${adminUser[3]}</td>
                    <td>${adminUser[4]}</td>
                    <td>
                        <button onclick='editUser(${adminUser[0]}, "${adminUser[5]}", "${adminUser[5]}")'><i class="fa-regular fa-pen-to-square"></i></button>
                        <button onclick='deleteUser(${adminUser[0]}, "${adminUser[5]}")'><i class="fa-regular fa-trash-can"></i></button>
                    </td>
                `;
                tableBody.appendChild(row);
            });

    } catch (error) {
        console.error('An error occurred while receiving admin users data:', error);
    }
}
displayAdminUsers()
async function approveRegistration(id, role, table) {
  try {
      const response = await fetch(`${HOST}${table}/approve/${id}/${role}`, {
          method: 'PUT',
          headers: {
              'Content-Type': 'application/json',
          },
      });

      if (!response.ok) {
          throw new Error(`HTTP error! Status: ${response.status}`);
      }
      const data = await response.json();

      console.log('Користувач успішно підтверджений:', data);

      return data;
  } catch (error) {
      console.error('Помилка підтвердження користувача:', error);
  }
}


async function deleteUser(id, table) {
  try {
      const response = await fetch(`${HOST}${table}/${id}`, {
          method: 'DELETE',
          headers: {
              'Content-Type': 'application/json',
          },
      });

      if (!response.ok) {
          throw new Error(`HTTP error! Status: ${response.status}`);
      }
      const row = event.target.closest('tr');
      if (row) {
          row.remove();
      }

      console.log('Користувач успішно видалений');
  } catch (error) {
      console.error('Помилка видалення користувача:', error);
  }
}



async function getAllDefects() {
  try {
      let response = await fetch(HOST + "issues");
      let data = await response.json();
      const tableBody = document.querySelector('#defect-table tbody');

      data.forEach(issue => {
          const row = document.createElement('tr');
          row.innerHTML = `
              <td>${issue.telegramUser.firstName} ${issue.telegramUser.lastName}</td>
              <td>${issue.room}</td>
              <td>${issue.description}</td>
              <td><img src="images/${(issue.photo == null ? "default.jpg" : issue.photo.split('/')[5])}" alt="Фото відсутнє" style="max-width: 100px; max-height: 100px;"></td>
              <td>
                  <button onclick="deleteDefect(${issue.id})"><i class="fa-regular fa-trash-can"></i></button>
              </td>
          `;
          tableBody.appendChild(row);
      });

  } catch (error) {
      console.error('An error occurred while receiving all defects data:', error);
  }
}

getAllDefects();

async function deleteDefect(id) {
  try {
      const response = await fetch(`${HOST}issues/${id}`, {
          method: 'DELETE',
          headers: {
              'Content-Type': 'application/json',
          },
      });

      if (!response.ok) {
          throw new Error(`HTTP error! Status: ${response.status}`);
      }
      const row = event.target.closest('tr');
      if (row) {
          row.remove();
      }

      console.log('Користувач успішно видалений');
  } catch (error) {
      console.error('Помилка видалення дефекту:', error);
  }
}

function openSection(evt, sectionName) {
    var i, tabcontent, tablinks;
    tabcontent = document.getElementsByClassName("tabcontent");
    for (i = 0; i < tabcontent.length; i++) {
        tabcontent[i].style.display = "none";
    }
    tablinks = document.getElementsByClassName("tablinks");
    for (i = 0; i < tablinks.length; i++) {
        tablinks[i].className = tablinks[i].className.replace(" active", "");
    }
    var role = localStorage.getItem("role");

    if (role === "admin" || (role === "repair" && (sectionName === "Main" || sectionName === "Defects"))) {
        document.getElementById(sectionName).style.display = "block";
        evt.currentTarget.className += " active";
    } else {
        alert("Немає доступу до цієї вкладки для поточного користувача.");
    }
}

function fetchUserProfile(userId) {
    // Fetch user data from the server
    fetch(`${HOST}webUsers/${userId}`)
        .then(response => response.json())
        .then(data => {
            // Display user profile
            displayUserProfile(data);

            // Open the 'Main' section after redirecting
            openSection(event, 'Main');
        })
        .catch(error => {
            console.error('Error fetching user data:', error);
        });
}

function displayUserProfile(data) {
    // Your existing code for displaying the user profile
    var userPhotoElement = document.querySelector('.profile .user-photo');
    var userLoginElement = document.querySelector('.profile .user-login');
    var userRoleElement = document.querySelector('.profile .user-role');

    if (userPhotoElement && userLoginElement && userRoleElement) {
        // Display user photo
        userPhotoElement.style.backgroundImage = `url(images/${data.photoPath == null ? "defaultUserPhoto.jpg" : data.photoPath.split('\\')[5]})`;

        // Display user login
        if (data.login) {
            userLoginElement.textContent = data.login;
        }

        // Display user role
        if (data.role) {
            userRoleElement.textContent = `${data.role}`;
        }
    } else {
        console.error('Profile elements not found on the page.');
    }
}
if(window.location.href.split('/')[3] == "authorization.html") {
    document.querySelector('#loginForm form').addEventListener("submit", function(event) {
        event.preventDefault();
        fetch(`${HOST}webUsers/login?loginUsername=${document.getElementById('loginUsername').value}&loginPassword=${document.getElementById('loginPassword').value}`, {
                method: 'GET',
            })
            .then(response =>
                response.json()
            )
            .then(data => {
                if (data.id != null) {
                    if (data.activated) {
                        localStorage.setItem("role", data.role);
                        localStorage.setItem('userId', data.id)
                        window.location.href = 'index.html';
                    } else {
                        alert("Реєстрація ще не одобрена адміністратором");
                    }
                } else {
                    alert("Неправильний логін чи пароль");
                }
            })
            .catch(error => {
                console.error('Помилка реєстрації користувача:', error);
            });
    });
    
    
    
    
    document.querySelector('#registrationForm form').addEventListener("submit", function(event) {
      event.preventDefault(); 
        var formData = new FormData(this); 
        fetch(HOST + 'webUsers/upload', {
            method: 'POST',
            body: formData
        })
        .then(response => response.json())
        .then(data => {
          alert("Успішна реєстрація.")
            return data;
        })
        .catch(error => {
            console.error('Помилка реєстрації користувача:', error);
        });
    
    })
}






















// async function getIssueById(id) {
//    try {
//      const response = await fetch(HOST + 'issues/' + id)
//      const data = response.json();
//      return data;
//    } catch (error) {
//      console.error('An error occurred while receiving issue data:', error)
//    }
// }

