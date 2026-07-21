import React, { useState, useEffect } from 'react';
import { X, Sparkles } from 'lucide-react'; // Added Sparkles icon
import '../../styles/dashboard.scss';

const AddBookModal = ({ onClose, onSubmit, initialData }) => {
  // Static categories matching your backend seeder
  const categories = [
      { id: 1, name: 'Technology' },
      { id: 2, name: 'Science' },
      { id: 3, name: 'Fiction' },
      { id: 4, name: 'Self-help' },
      { id: 5, name: 'Other' }
  ];

  const [form, setForm] = useState({
    title: '',
    author: '',
    isbn: '',
    publicationYear: '',
    totalCopies: 1,
    categoryId: 1 
  });
  const [fetching, setFetching] = useState(false);

  // Load initial data if editing
  useEffect(() => {
    if (initialData) {
      setForm({
        title: initialData.title || '',
        author: initialData.author || '',
        isbn: initialData.isbn || '',
        publicationYear: initialData.publicationYear || '',
        totalCopies: initialData.totalCopies || 1,
        categoryId: initialData.category?.categoryId || 1
      });
    }
  }, [initialData]);

  // --- ISBN LOOKUP HANDLER ---
  const handleIsbnLookup = async () => {
    if (!form.isbn) {
      alert("Please enter an ISBN first.");
      return;
    }
    
    setFetching(true);
    try {
      const cleanIsbn = form.isbn.replace(/[-\s]/g, '');
      const res = await fetch(`https://openlibrary.org/api/books?bibkeys=ISBN:${cleanIsbn}&format=json&jscmd=data`);
      if (res.ok) {
        const data = await res.json();
        const bookKey = `ISBN:${cleanIsbn}`;
        const bookInfo = data[bookKey];
        
        if (bookInfo) {
          let year = '';
          if (bookInfo.publish_date) {
            const match = bookInfo.publish_date.match(/\d{4}/);
            if (match) year = parseInt(match[0]);
          }
          
          const authorName = bookInfo.authors?.map(a => a.name).join(', ') || '';
          
          setForm(prev => ({
            ...prev,
            title: bookInfo.title || prev.title,
            author: authorName || prev.author,
            publicationYear: year || prev.publicationYear,
          }));
        } else {
          alert("No book details found for this ISBN. Please enter details manually.");
        }
      } else {
        alert("Failed to connect to the lookup service. Please fill details manually.");
      }
    } catch (err) {
      console.error(err);
      alert("Error looking up ISBN. Please enter details manually.");
    } finally {
      setFetching(false);
    }
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    onSubmit(form);
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-container" onClick={(e) => e.stopPropagation()}>
        
        <div className="modal-header">
          <div className="modal-title">
            <h2>{initialData ? 'Edit Book' : 'Add Book'}</h2>
            <p>{initialData ? 'Update book details' : 'List a book or resource'}</p>
          </div>
          <button className="btn-close" onClick={onClose}>
            <X size={20} />
          </button>
        </div>

        <div className="modal-body">
          
          {/* --- ISBN LOOKUP TOOL (Only for Add Mode) --- */}
          {!initialData && (
            <div style={{ marginBottom: '20px', padding: '12px', background: 'var(--beige-bg)', border: '1px solid var(--border-light)', borderRadius: '8px' }}>
              <label style={{ display: 'flex', alignItems: 'center', gap: '6px', fontSize: '13px', fontWeight: '600', color: 'var(--maroon)', marginBottom: '8px' }}>
                <Sparkles size={14} /> Auto-fill from ISBN
              </label>
              <div style={{ display: 'flex', gap: '8px' }}>
                <input 
                  type="text"
                  className="form-input" 
                  placeholder="Enter ISBN (e.g., 9780132350884)"
                  value={form.isbn}
                  onChange={(e) => setForm({ ...form, isbn: e.target.value })}
                  style={{ margin: 0 }}
                />
                <button 
                  type="button"
                  onClick={handleIsbnLookup}
                  className="btn-confirm"
                  disabled={fetching}
                  style={{ whiteSpace: 'nowrap', padding: '10px 16px', margin: 0, backgroundColor: 'var(--maroon)' }}
                >
                  {fetching ? 'Searching...' : 'Lookup & Fill'}
                </button>
              </div>
              <p style={{ fontSize: '11px', color: 'var(--text-sub)', marginTop: '6px', marginBottom: 0 }}>
                Query the Open Library database to automatically populate Title, Author, and Publication Year.
              </p>
            </div>
          )}

          <form id="add-book-form" onSubmit={handleSubmit}>
            <div className="form-group">
              <label className="form-label">Item Name *</label>
              <input 
                className="form-input" 
                placeholder="e.g., Clean Code"
                value={form.title}
                onChange={(e) => setForm({ ...form, title: e.target.value })}
                required
              />
            </div>

            <div className="form-row">
              <div className="form-group">
                <label className="form-label">Author / Brand *</label>
                <input 
                  className="form-input" 
                  placeholder="e.g., Robert C. Martin"
                  value={form.author}
                  onChange={(e) => setForm({ ...form, author: e.target.value })}
                  required
                />
              </div>

              <div className="form-group">
                <label className="form-label">Category *</label>
                <select 
                  className="form-input"
                  value={form.categoryId}
                  onChange={(e) => setForm({ ...form, categoryId: parseInt(e.target.value) })}
                >
                  {categories.map(cat => (
                    <option key={cat.id} value={cat.id}>{cat.name}</option>
                  ))}
                </select>
              </div>
            </div>

            <div className="form-row">
               <div className="form-group">
                <label className="form-label">ISBN / Serial No. *</label>
                <input 
                  className="form-input" 
                  placeholder="e.g., 978-0132350884"
                  value={form.isbn}
                  onChange={(e) => setForm({ ...form, isbn: e.target.value })}
                  required
                />
              </div>

              <div className="form-group">
                <label className="form-label">Year</label>
                <input 
                  type="number"
                  className="form-input" 
                  placeholder="2024"
                  value={form.publicationYear}
                  onChange={(e) => setForm({ ...form, publicationYear: e.target.value })}
                  required
                />
              </div>
            </div>

            <div className="form-group">
              <label className="form-label">Total Copies *</label>
              <input 
                type="number"
                min="1"
                className="form-input" 
                value={form.totalCopies}
                onChange={(e) => setForm({ ...form, totalCopies: parseInt(e.target.value) })}
                required
              />
            </div>
          </form>
        </div>

        <div className="modal-footer">
          <button className="btn-cancel" onClick={onClose}>Cancel</button>
          <button type="submit" form="add-book-form" className="btn-confirm">
            {initialData ? 'Save Changes' : 'Add Item'}
          </button>
        </div>

      </div>
    </div>
  );
};

export default AddBookModal;