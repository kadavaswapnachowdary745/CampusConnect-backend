import React, { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import api from '../api';

const EditProduct = () => {
  const { id } = useParams();
  const [product, setProduct] = useState(null);
  const [image, setImage] = useState(null);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    const loadProduct = async () => {
      try {
        const res = await api.get(`products/${id}`);
        setProduct(res.data);
      } catch (err) {
        setError('Could not load product');
      }
    };
    loadProduct();
  }, [id]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const formData = new FormData();
      formData.append('title', product.title);
      formData.append('description', product.description);
      formData.append('price', product.price);
      formData.append('category', product.category);
      if (image) formData.append('image', image);

      await api.put(`products/${id}`, formData, { headers: { 'Content-Type': 'multipart/form-data' } });
      navigate(`/product/${id}`);
    } catch (err) {
      setError('Could not update product');
    }
  };

  if (error) return <p>{error}</p>;
  if (!product) return <p>Loading...</p>;

  return (
    <div>
      <h2>Edit Product</h2>
      <form onSubmit={handleSubmit}>
        <input value={product.title} onChange={(e) => setProduct({ ...product, title: e.target.value })} required />
        <textarea value={product.description} onChange={(e) => setProduct({ ...product, description: e.target.value })} required />
        <input type="number" value={product.price} onChange={(e) => setProduct({ ...product, price: e.target.value })} min="0" required />
        <select value={product.category} onChange={(e) => setProduct({ ...product, category: e.target.value })}>
          <option value="BOOKS">Books</option>
          <option value="ELECTRONICS">Electronics</option>
          <option value="LAB_EQUIPMENT">Lab Equipment</option>
          <option value="HOSTEL_ESSENTIALS">Hostel Essentials</option>
          <option value="ACCESSORIES">Accessories</option>
          <option value="PROJECT_MATERIALS">Project Materials</option>
          <option value="OTHER">Other</option>
        </select>
        <input type="file" onChange={(e) => setImage(e.target.files?.[0] || null)} />
        <button type="submit">Save Changes</button>
      </form>
    </div>
  );
};

export default EditProduct;
