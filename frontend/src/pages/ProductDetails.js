import React, { useEffect, useState } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import api from '../api';

const ProductDetails = () => {
  const { id } = useParams();
  const [product, setProduct] = useState(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);
  const userId = Number(localStorage.getItem('userId'));
  const userEmail = localStorage.getItem('userEmail');
  const userName = localStorage.getItem('userName');
  const navigate = useNavigate();

  useEffect(() => {
    const fetchProduct = async () => {
      try {
        setLoading(true);
        const res = await api.get(`products/${id}`);
        setProduct(res.data);
      } catch (err) {
        setError('Product not found');
      } finally {
        setLoading(false);
      }
    };
    fetchProduct();
  }, [id]);

  const handleDelete = async () => {
    if (!window.confirm('Are you sure you want to delete this product?')) return;
    try {
      await api.delete(`products/${id}`);
      navigate('/');
    } catch (err) {
      setError('Could not delete product');
    }
  };

  const addToWishlist = async () => {
    try {
      await api.post(`wishlist/${id}`);
      alert('Added to wishlist successfully!');
    } catch (err) {
      alert('Failed to add to wishlist');
    }
  };

  const handleBuy = async () => {
    try {
      const { data } = await api.post('payments/create-order', { productId: id });

      if (!window.Razorpay) {
        toast.error('Razorpay checkout script not loaded.');
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
          name: userName || '',
          email: userEmail || '',
        },
        handler: async (response) => {
          try {
            await api.post('payments/verify', {
              razorpayOrderId: response.razorpay_order_id,
              razorpayPaymentId: response.razorpay_payment_id,
              razorpaySignature: response.razorpay_signature,
            });
            setProduct({ ...product, sold: true });
            toast.success('Payment successful! Product marked as sold.');
          } catch (err) {
            toast.error(err.response?.data?.message || 'Payment verification failed.');
          }
        },
        theme: {
          color: '#2563eb',
        },
      };

      const rzp = new window.Razorpay(options);
      rzp.open();
    } catch (err) {
      const message = err.response?.data?.message || 'Could not create payment order.';
      toast.error(message);
    }
  };

  const contactSeller = () => {
    navigate(`/chat?user=${product.sellerId}`);
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center py-12">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="text-center py-12">
        <svg className="mx-auto h-24 w-24 text-red-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L4.082 16.5c-.77.833.192 2.5 1.732 2.5z" />
        </svg>
        <h3 className="mt-4 text-lg font-medium text-gray-900">Error</h3>
        <p className="mt-2 text-gray-500">{error}</p>
        <Link to="/" className="mt-4 inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700">
          Back to Marketplace
        </Link>
      </div>
    );
  }

  if (!product) return null;

  const isOwner = userId === product.sellerId;

  return (
    <div className="max-w-6xl mx-auto">
      <div className="bg-white rounded-lg shadow-lg overflow-hidden">
        <div className="md:flex">
          {/* Product Image */}
          <div className="md:w-1/2">
            <div className="relative">
              {product.imagePath ? (
                <img
                  src={`http://localhost:8080${product.imagePath}`}
                  alt={product.title}
                  className="w-full h-96 md:h-full object-cover"
                />
              ) : (
                <div className="w-full h-96 md:h-full bg-gray-200 flex items-center justify-center">
                  <svg className="h-24 w-24 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1} d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                  </svg>
                </div>
              )}

              {/* Status Badge */}
              <div className={`absolute top-4 right-4 px-3 py-1 rounded-full text-sm font-medium ${
                product.sold ? 'bg-red-500 text-white' : 'bg-green-500 text-white'
              }`}>
                {product.sold ? 'Sold' : 'Available'}
              </div>
            </div>
          </div>

          {/* Product Details */}
          <div className="md:w-1/2 p-8">
            <div className="mb-6">
              <h1 className="text-3xl font-bold text-gray-900 mb-2">{product.title}</h1>
              <div className="flex items-center space-x-4 mb-4">
                <span className="text-3xl font-bold text-blue-600">₹{product.price}</span>
                <span className="text-sm text-gray-500 bg-gray-100 px-3 py-1 rounded-full">
                  {product.category}
                </span>
              </div>
            </div>

            {/* Seller Information */}
            <div className="bg-gray-50 rounded-lg p-4 mb-6">
              <h3 className="text-lg font-semibold text-gray-900 mb-2">Seller Information</h3>
              <p className="text-gray-700">
                <span className="font-medium">Name:</span> {product.sellerName}
              </p>
              <p className="text-gray-700">
                <span className="font-medium">Contact:</span> Available via chat
              </p>
            </div>

            {/* Description */}
            <div className="mb-6">
              <h3 className="text-lg font-semibold text-gray-900 mb-2">Description</h3>
              <p className="text-gray-700 leading-relaxed">{product.description}</p>
            </div>

            {/* Action Buttons */}
            <div className="space-y-3">
              {isOwner ? (
                <div className="flex gap-3">
                  <Link
                    to={`/edit/${id}`}
                    className="flex-1 bg-blue-600 hover:bg-blue-700 text-white px-6 py-3 rounded-lg font-medium text-center transition duration-200"
                  >
                    Edit Product
                  </Link>
                  <button
                    onClick={handleDelete}
                    className="flex-1 bg-red-600 hover:bg-red-700 text-white px-6 py-3 rounded-lg font-medium transition duration-200"
                  >
                    Delete Product
                  </button>
                </div>
              ) : (
                !product.sold && (
                  <div className="flex gap-3">
                    <button
                      onClick={handleBuy}
                      className="flex-1 bg-green-600 hover:bg-green-700 text-white px-6 py-3 rounded-lg font-medium transition duration-200"
                    >
                      Buy Now
                    </button>
                    <button
                      onClick={contactSeller}
                      className="flex-1 bg-blue-600 hover:bg-blue-700 text-white px-6 py-3 rounded-lg font-medium transition duration-200"
                    >
                      Contact Seller
                    </button>
                  </div>
                )
              )}

              {!isOwner && (
                <button
                  onClick={addToWishlist}
                  className="w-full bg-gray-100 hover:bg-gray-200 text-gray-800 px-6 py-3 rounded-lg font-medium transition duration-200 flex items-center justify-center space-x-2"
                >
                  <svg className="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
                  </svg>
                  <span>Add to Wishlist</span>
                </button>
              )}

              {product.sold && !isOwner && (
                <div className="bg-red-50 border border-red-200 rounded-lg p-4 text-center">
                  <p className="text-red-800 font-medium">This product has already been sold.</p>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>

      {/* Back Button */}
      <div className="mt-6">
        <Link
          to="/"
          className="inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 transition duration-200"
        >
          <svg className="mr-2 h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
          </svg>
          Back to Marketplace
        </Link>
      </div>
    </div>
  );
};

export default ProductDetails;
