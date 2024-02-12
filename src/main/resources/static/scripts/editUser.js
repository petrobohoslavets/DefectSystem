// JavaScript (editUser.js)
const urlParams = new URLSearchParams(window.location.search);
const userId = urlParams.get('userId');
const table = urlParams.get('table');
document.addEventListener('DOMContentLoaded', function () {
    fetch(HOST + table + "/" + userId)
    .then(response => response.json())
    .then(data => {
        const firstName = data.firstName;
        const lastName = data.lastName;
        const permission = data.role;
        // const confirmed = data.activated;

        // Populate the form with user data
        document.getElementById('userId').value = userId;
        document.getElementById('firstName').value = firstName;
        document.getElementById('lastName').value = lastName;
        document.getElementById('permission').value = permission;
        // document.getElementById('confirmed').checked = confirmed;
    })
    .catch(error => console.error('An error occurred while receiving user data:', error));
    
});

async function saveChanges() {
    // Get the edited data from the form
    const updatedFirstName = document.getElementById('firstName').value;
    const updatedLastName = document.getElementById('lastName').value;
    const updatedPermission = document.getElementById('permission').value;
    // const updatedConfirmed = document.getElementById('confirmed').checked;
    
    // Update the data on the server
    await updateUserDataOnServer(userId, updatedFirstName, updatedLastName, updatedPermission);

    // Redirect back to the main page or perform other actions
    window.location.href = 'index.html';
}

async function updateUserDataOnServer(userId, updatedFirstName, updatedLastName, updatedPermission) {
    try {
        let json = {};
        if(userId > 99999999) {
            json = JSON.stringify({
                id: userId,
                firstName: updatedFirstName,
                lastName: updatedLastName,
                role: updatedPermission,
            })
        } else {
            let userData = await getUserById(userId, 'webUsers')
            json = JSON.stringify({
                id: userId,
                login: userData.login,
                password: userData.password,
                photoPath: userData.photoPath,
                firstName: updatedFirstName,
                lastName: updatedLastName,
                role: updatedPermission,
                activated: userData.activated,
            })
        }
        console.log(json)
        const response = await fetch(HOST + table, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
            },
            body: json
        });

        if (!response.ok) {
            throw new Error('Failed to update user data on the server');
        }

        console.log('User data updated on the server');
    } catch (error) {
        console.error('Error updating user data on the server:', error);
    }
}
