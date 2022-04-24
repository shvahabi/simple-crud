function submitFormData(event, actionTarget) {
event.preventDefault();
const xhr = new XMLHttpRequest();
const data = new FormData(event.target.parentNode);
const formAsJson = JSON.stringify(Object.fromEntries(data.entries()));
xhr.open('POST', actionTarget);
xhr.setRequestHeader('Content-Type', 'application/json');
xhr.send(formAsJson);
}