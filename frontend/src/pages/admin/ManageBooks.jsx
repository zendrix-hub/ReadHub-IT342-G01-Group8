import React, { useState, useEffect, useCallback } from 'react'; // 1. Import useCallback
import DashboardLayout from '../../components/layout/DashboardLayout';
import BookCard from '../../components/cards/BookCard';
import PrimaryButton from '../../components/buttons/PrimaryButton';
import SecondaryButton from '../../components/buttons/SecondaryButton';
import TextInput from '../../components/inputs/TextInput';
import { Plus, X } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import { useToast } from '../../context/ToastContext'; // Import Toast

const ManageBooks = () => {
  const [books, setBooks] = useState([]);
  const [showModal, setShowModal] = useState(false);
  const [form, setForm] = useState({ title: '', author: '', isbn: '', publicationYear: '', totalCopies: 1, categoryId: 1 });
  const { token } = useAuth();
  const { showToast } = useToast();

  // 2. Wrap fetchBooks in useCallback
  const fetchBooks = useCallback(() => {
    fetch('http://localhost:8080/api/books', {
      headers: { Authorization: `Bearer ${token}` }
    })
    .then(res => res.json())
    .then(data => setBooks(data.data || data))
    .catch(() => showToast("Failed to load books", "error"));
  }, [token, showToast]); 

  // 3. Add fetchBooks to dependency array
  useEffect(() => { 
    fetchBooks(); 
  }, [fetchBooks]); 

  const handleAddBook = async (e) => {
    e.preventDefault();
    const res = await fetch('http://localhost:8080/api/books', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` },
      body: JSON.stringify(form)
    });
    if (res.ok) {
      showToast('Book Added Successfully', 'success'); // Using Toast
      setShowModal(false);
      fetchBooks();
    } else {
      showToast('Failed to add book', 'error');
    }
  };

  return (
    <DashboardLayout 
      title="Inventory Management" 
      actions={<PrimaryButton style={{width: 'auto'}} onClick={() => setShowModal(true)}><Plus size={18}/> Add Book</PrimaryButton>}
    >
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))', gap: '24px' }}>
        {books.map(book => (
          <BookCard key={book.bookId} book={book} isAdmin={true} />
        ))}
      </div>

      {showModal && (
        <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, background: 'rgba(0,0,0,0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
          <div style={{ background: 'white', padding: '32px', borderRadius: '8px', width: '500px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '24px' }}>
              <h3>Add New Book</h3>
              <button onClick={() => setShowModal(false)} style={{ background: 'none', border: 'none', cursor: 'pointer' }}><X /></button>
            </div>
            <form onSubmit={handleAddBook}>
              <TextInput label="Title" value={form.title} onChange={e => setForm({...form, title: e.target.value})} />
              <div className="flex gap-md">
                <TextInput label="Author" value={form.author} onChange={e => setForm({...form, author: e.target.value})} />
                <TextInput label="ISBN" value={form.isbn} onChange={e => setForm({...form, isbn: e.target.value})} />
              </div>
              <div className="flex gap-md">
                <TextInput label="Year" type="number" value={form.publicationYear} onChange={e => setForm({...form, publicationYear: e.target.value})} />
                <TextInput label="Copies" type="number" value={form.totalCopies} onChange={e => setForm({...form, totalCopies: e.target.value})} />
              </div>
              <div className="flex gap-md mt-4">
                <SecondaryButton type="button" onClick={() => setShowModal(false)}>Cancel</SecondaryButton>
                <PrimaryButton type="submit">Save Book</PrimaryButton>
              </div>
            </form>
          </div>
        </div>
      )}
    </DashboardLayout>
  );
};

export default ManageBooks;