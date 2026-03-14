import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import api from '../api';

const ProductCard = ({ product, onRefresh }) => {
  const navigate = useNavigate();
  const currentUserId = Number(localStorage.getItem('userId'));
  const isOwner = currentUserId === product.sellerId;

  const handleDelete = async () => {
    if (!window.confirm('Are you sure you want to delete this product?')) return;
    try {
      await api.delete(`products/${product.id}`);
      if (onRefresh) onRefresh();
      else window.location.reload();
    } catch (err) {
      alert(err.response?.data?.message || 'Could not delete product');
    }
  };

  const handleBuy = async () => {
    try {
      const { data } = await api.post('payments/create-order', { productId: product.id });

      if (!window.Razorpay) {
        alert('Razorpay checkout script not loaded.');
        return;
      }

      const options = {
        key: data.keyId,
        amount: data.amount,
        currency: data.currency,
        name: product.title,
        description: `Purchase - ${product.title}`,
        order_id: data.orderId,
        prefill: {
          name: localStorage.getItem('userName') || '',
          email: localStorage.getItem('userEmail') || '',
        },
        handler: async (response) => {
          try {
            await api.post('payments/verify', {
              razorpayOrderId: response.razorpay_order_id,
              razorpayPaymentId: response.razorpay_payment_id,
              razorpaySignature: response.razorpay_signature,
            });
            if (onRefresh) onRefresh();
            else window.location.reload();
          } catch (err) {
            alert(err.response?.data?.message || 'Payment verification failed.');
          }
        },
      };

      const rzp = new window.Razorpay(options);
      rzp.open();
    } catch (err) {
      alert(err.response?.data?.message || 'Unable to initiate payment');
    }
  };

  const handleContactSeller = () => {
    navigate(`/chat?user=${product.sellerId}`);
  };

  return (
    <div className="bg-white rounded-lg shadow-md hover:shadow-lg transition-shadow duration-300 overflow-hidden border border-gray-200">
      {/* Product Image */}
      <div className="relative">
        {product.imagePath ? (
          <img
            src={`http://localhost:8080${product.imagePath}`}
            alt={product.title}
            className="w-full h-48 object-cover hover:scale-105 transition-transform duration-300"
          />
        ) : (
          <div className="w-full h-48 bg-gray-200 flex items-center justify-center">
            <svg className="h-12 w-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1} d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
            </svg>
          </div>
        )}

        {/* Status Badge */}
        <div className={`absolute top-2 right-2 px-2 py-1 rounded-full text-xs font-medium ${
          product.sold ? 'bg-red-500 text-white' : 'bg-green-500 text-white'
        }`}>
          {product.sold ? 'Sold' : 'Available'}
        </div>
      </div>

      {/* Product Info */}
      <div className="p-4">
        <h3 className="text-lg font-semibold text-gray-900 mb-1 line-clamp-2 hover:text-blue-600 transition-colors">
          {product.title}
        </h3>

        <div className="flex items-center justify-between mb-2">
          <span className="text-2xl font-bold text-blue-600">₹{product.price}</span>
          <span className="text-sm text-gray-500 bg-gray-100 px-2 py-1 rounded-full">
            {product.category}
          </span>
        </div>

        <p className="text-sm text-gray-600 mb-3">
          Seller: <span className="font-medium">{product.sellerName}</span>
        </p>

        {/* Action Buttons */}
        <div className="flex gap-2">
          <Link
            to={`/product/${product.id}`}
            className="flex-1 bg-gray-100 hover:bg-gray-200 text-gray-800 px-3 py-2 rounded-lg text-center text-sm font-medium transition-colors duration-200"
          >
            View Details
          </Link>

          {isOwner ? (
            <div className="flex gap-1">
              <Link
                to={`/edit/${product.id}`}
                className="bg-blue-500 hover:bg-blue-600 text-white px-3 py-2 rounded-lg text-sm font-medium transition-colors duration-200"
              >
                Edit
              </Link>
              <button
                onClick={handleDelete}
                className="bg-red-500 hover:bg-red-600 text-white px-3 py-2 rounded-lg text-sm font-medium transition-colors duration-200"
              >
                Delete
              </button>
            </div>
          ) : (
            !product.sold && (
              <div className="flex gap-1">
                <button
                  onClick={handleBuy}
                  className="bg-green-500 hover:bg-green-600 text-white px-3 py-2 rounded-lg text-sm font-medium transition-colors duration-200"
                >
                  Buy
                </button>
                <button
                  onClick={handleContactSeller}
                  className="bg-blue-500 hover:bg-blue-600 text-white px-3 py-2 rounded-lg text-sm font-medium transition-colors duration-200"
                >
                  Contact
                </button>
              </div>
            )
          )}
        </div>
      </div>
    </div>
  );
};

export default ProductCard;
