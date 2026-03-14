import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import api from '../api';

const AddProduct = () => {
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [price, setPrice] = useState('');
  const [category, setCategory] = useState('BOOKS');
  const [condition, setCondition] = useState('LIKE_NEW'); // Added condition state
  const [image, setImage] = useState(null);
  const [imagePreview, setImagePreview] = useState(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [aiGeneratingDesc, setAiGeneratingDesc] = useState(false);
  const [aiSuggestingPrice, setAiSuggestingPrice] = useState(false);
  const [priceSuggestion, setPriceSuggestion] = useState('');
  const navigate = useNavigate();

  const conditions = [
    { value: 'NEW', label: 'New' },
    { value: 'LIKE_NEW', label: 'Like New' },
    { value: 'GOOD', label: 'Good' },
    { value: 'FAIR', label: 'Fair' },
    { value: 'POOR', label: 'Poor' }
  ];

  const categories = [
    { value: 'BOOKS', label: 'Books' },
    { value: 'ELECTRONICS', label: 'Electronics' },
    { value: 'LAB_EQUIPMENT', label: 'Lab Equipment' },
    { value: 'HOSTEL_ESSENTIALS', label: 'Hostel Essentials' },
    { value: 'ACCESSORIES', label: 'Accessories' },
    { value: 'PROJECT_MATERIALS', label: 'Project Materials' },
    { value: 'OTHER', label: 'Other' }
  ];

  const handleImageChange = (e) => {
    const file = e.target.files?.[0];
    if (file) {
      setImage(file);
      const reader = new FileReader();
      reader.onload = (e) => setImagePreview(e.target.result);
      reader.readAsDataURL(file);
    }
  };

  const handleGenerateDescription = async () => {
    if (!title.trim()) {
      toast.warning('Please enter a title to generate a description.');
      return;
    }
    try {
      setAiGeneratingDesc(true);
      const response = await api.post('/ai/generate-description', {
        title,
        category,
        condition
      });
      setDescription(response.data.text);
      toast.success('Description generated!');
    } catch (err) {
      console.error(err);
      toast.error('Failed to generate description with AI.');
    } finally {
      setAiGeneratingDesc(false);
    }
  };

  const handleSuggestPrice = async () => {
    if (!title.trim()) {
      toast.warning('Please enter a title to get a price suggestion.');
      return;
    }
    try {
      setAiSuggestingPrice(true);
      const response = await api.post('/ai/suggest-price', {
        title,
        condition
      });
      setPriceSuggestion(response.data.text);
      toast.success('Price suggested!');
    } catch (err) {
      console.error(err);
      toast.error('Failed to suggest price with AI.');
    } finally {
      setAiSuggestingPrice(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!title.trim() || !description.trim() || !price) {
      setError('Please fill in all required fields');
      return;
    }

    if (parseFloat(price) <= 0) {
      setError('Price must be greater than 0');
      return;
    }

    try {
      setLoading(true);
      setError('');
      const formData = new FormData();
      formData.append('title', title.trim());
      formData.append('description', description.trim());
      formData.append('price', price);
      formData.append('category', category);
      formData.append('condition', condition); // Added condition to form data
      if (image) formData.append('image', image);

      await api.post('products', formData, { headers: { 'Content-Type': 'multipart/form-data' } });
      toast.success('Product added successfully!');
      navigate('/');
    } catch (err) {
      const message = err.response?.data?.message || 'Could not add product';
      setError(message);
      toast.error(message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-2xl mx-auto">
      <div className="bg-white rounded-lg shadow-lg overflow-hidden">
        {/* Header */}
        <div className="bg-gradient-to-r from-blue-600 to-blue-700 px-6 py-4">
          <h1 className="text-2xl font-bold text-white">Add New Product</h1>
          <p className="text-blue-100 mt-1">List your item for sale in the campus marketplace</p>
        </div>

        {/* Form */}
        <form onSubmit={handleSubmit} className="p-6 space-y-6">
          {/* Error Message */}
          {error && (
            <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg text-sm">
              <div className="flex items-center">
                <svg className="h-4 w-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L4.082 16.5c-.77.833.192 2.5 1.732 2.5z" />
                </svg>
                {error}
              </div>
            </div>
          )}

          {/* Title */}
          <div>
            <label htmlFor="title" className="block text-sm font-medium text-gray-700 mb-2">
              Product Title *
            </label>
            <input
              id="title"
              type="text"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent transition duration-200"
              placeholder="Enter product title"
              required
            />
          </div>

          {/* Description */}
          <div>
            <div className="flex justify-between items-center mb-2">
              <label htmlFor="description" className="block text-sm font-medium text-gray-700">
                Description *
              </label>
              <button
                type="button"
                onClick={handleGenerateDescription}
                disabled={aiGeneratingDesc || !title.trim()}
                className="flex items-center text-sm bg-indigo-50 text-indigo-600 hover:bg-indigo-100 px-3 py-1.5 rounded-md transition duration-200 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {aiGeneratingDesc ? (
                  <span className="animate-spin mr-2">✨</span>
                ) : (
                  <span className="mr-2">✨</span>
                )}
                Generate with AI
              </button>
            </div>
            <textarea
              id="description"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              rows={4}
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent transition duration-200 resize-none"
              placeholder="Describe your product in detail"
              required
            />
          </div>

          {/* Price, Category, Condition Row */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div>
              <div className="flex justify-between items-center mb-2">
                <label htmlFor="price" className="block text-sm font-medium text-gray-700">
                  Price (₹) *
                </label>
                <button
                  type="button"
                  onClick={handleSuggestPrice}
                  disabled={aiSuggestingPrice || !title.trim()}
                  className="text-xs text-indigo-600 hover:text-indigo-800"
                >
                  {aiSuggestingPrice ? 'Suggesting...' : '✨ Suggest AI Price'}
                </button>
              </div>
              <input
                id="price"
                type="number"
                value={price}
                onChange={(e) => setPrice(e.target.value)}
                min="0"
                step="0.01"
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent transition duration-200"
                placeholder="0.00"
                required
              />
            </div>

            <div>
              <label htmlFor="category" className="block text-sm font-medium text-gray-700 mb-2">
                Category *
              </label>
              <select
                id="category"
                value={category}
                onChange={(e) => setCategory(e.target.value)}
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent transition duration-200 bg-white"
              >
                {categories.map((cat) => (
                  <option key={cat.value} value={cat.value}>
                    {cat.label}
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label htmlFor="condition" className="block text-sm font-medium text-gray-700 mb-2">
                Condition *
              </label>
              <select
                id="condition"
                value={condition}
                onChange={(e) => setCondition(e.target.value)}
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent transition duration-200 bg-white"
              >
                {conditions.map((cond) => (
                  <option key={cond.value} value={cond.value}>
                    {cond.label}
                  </option>
                ))}
              </select>
            </div>
          </div>

          {/* AI Price Suggestion display */}
          {priceSuggestion && (
            <div className="bg-indigo-50 border-l-4 border-indigo-500 p-4 rounded-r-lg">
              <div className="flex">
                <div className="flex-shrink-0">
                  <span className="text-indigo-500 pt-1 block">✨</span>
                </div>
                <div className="ml-3">
                  <h3 className="text-sm font-medium text-indigo-800">AI Price Suggestion</h3>
                  <div className="mt-2 text-sm text-indigo-700 whitespace-pre-wrap">
                    {priceSuggestion}
                  </div>
                </div>
              </div>
            </div>
          )}

          {/* Image Upload */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Product Image
            </label>
            <div className="mt-1 flex justify-center px-6 pt-5 pb-6 border-2 border-gray-300 border-dashed rounded-lg hover:border-gray-400 transition duration-200">
              <div className="space-y-1 text-center">
                {imagePreview ? (
                  <div className="mx-auto">
                    <img
                      src={imagePreview}
                      alt="Preview"
                      className="mx-auto h-32 w-32 object-cover rounded-lg"
                    />
                    <button
                      type="button"
                      onClick={() => {
                        setImage(null);
                        setImagePreview(null);
                      }}
                      className="mt-2 text-sm text-red-600 hover:text-red-800"
                    >
                      Remove image
                    </button>
                  </div>
                ) : (
                  <>
                    <svg
                      className="mx-auto h-12 w-12 text-gray-400"
                      stroke="currentColor"
                      fill="none"
                      viewBox="0 0 48 48"
                      aria-hidden="true"
                    >
                      <path
                        d="M28 8H12a4 4 0 00-4 4v20m32-12v8m0 0v8a4 4 0 01-4 4H12a4 4 0 01-4-4v-4m32-4l-3.172-3.172a4 4 0 00-5.656 0L28 28M8 32l9.172-9.172a4 4 0 015.656 0L28 28m0 0l4 4m4-24h8m-4-4v8m-12 4h.02"
                        strokeWidth={2}
                        strokeLinecap="round"
                        strokeLinejoin="round"
                      />
                    </svg>
                    <div className="flex text-sm text-gray-600">
                      <label
                        htmlFor="image-upload"
                        className="relative cursor-pointer bg-white rounded-md font-medium text-blue-600 hover:text-blue-500 focus-within:outline-none"
                      >
                        <span>Upload a photo</span>
                        <input
                          id="image-upload"
                          name="image-upload"
                          type="file"
                          accept="image/*"
                          onChange={handleImageChange}
                          className="sr-only"
                        />
                      </label>
                      <p className="pl-1">or drag and drop</p>
                    </div>
                    <p className="text-xs text-gray-500">PNG, JPG, GIF up to 10MB</p>
                  </>
                )}
              </div>
            </div>
          </div>

          {/* Submit Button */}
          <div className="flex gap-4">
            <button
              type="button"
              onClick={() => navigate('/')}
              className="flex-1 bg-gray-100 hover:bg-gray-200 text-gray-800 px-6 py-3 rounded-lg font-medium transition duration-200"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={loading}
              className="flex-1 bg-blue-600 hover:bg-blue-700 disabled:bg-blue-400 disabled:cursor-not-allowed text-white px-6 py-3 rounded-lg font-medium transition duration-200 flex items-center justify-center"
            >
              {loading ? (
                <>
                  <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2"></div>
                  Adding Product...
                </>
              ) : (
                'Add Product'
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default AddProduct;
