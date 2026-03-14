import React, { useEffect, useState } from 'react';
import api from '../api';

const Wishlist = () => {
  const [items, setItems] = useState([]);
  const [error, setError] = useState('');

  useEffect(() => {
    const loadWishlist = async () => {
      try {
        const res = await api.get('wishlist');
        setItems(res.data);
      } catch (err) {
        setError('Could not load wishlist');
      }
    };
    loadWishlist();
  }, []);

  const removeItem = async (productId) => {
    try {
      await api.delete(`wishlist/${productId}`);
      setItems(items.filter((w) => w.product.id !== productId));
    } catch (err) {
      console.error(err);
    }
  };

  if (error) return <p>{error}</p>;

  return (
    <div>
      <h2>Wishlist</h2>
      {items.length === 0 ? <p>No items in wishlist</p> : items.map((w) => (
        <div key={w.id} style={{ border: '1px solid #ccc', padding: 8, marginBottom: 8 }}>
          <h4>{w.product.title}</h4>
          <p>{w.product.description}</p>
          <p>Price: ₹{w.product.price}</p>
          <button onClick={() => removeItem(w.product.id)}>Remove</button>
        </div>
      ))}
    </div>
  );
};

export default Wishlist;
