function listBookAuthors(books) {
    let listOfAuthors = [];
    books.forEach(function () {
        if (!listOfAuthors.includes(book.author)) {
            listOfAuthors.push(book.author);
        }
    });
    return listOfAuthors;
}

let myBooks = [
    {title: 'Harry Potter', author: 'J. K. Rowling'},
    {title: 'Lord of the Rings', author: 'J. R. R. Tolkien'},
    {title: 'The Hobbit', author: 'J. R. R. Tolkien'}
];
listBookAuthors(myBooks);