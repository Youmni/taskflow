import React, { createContext, useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import Cookies from 'js-cookie';

export const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [accessToken, setAccessToken] = useState(Cookies.get('accessToken') || null);
  const [refreshToken, setRefreshToken] = useState(Cookies.get('refreshToken') || null);
  const navigate = useNavigate();
  

  useEffect(() => {
    if (!accessToken && !refreshToken) {
      navigate('/login');
    }
  }, [accessToken, refreshToken]);

  useEffect(() => {
    const responseInterceptor = axios.interceptors.response.use(
      (response) => response,
      async (error) => {
        if (error.response && error.response.status === 403 && refreshToken) {
          try {
            const newTokens = await refreshAccessToken();
            setAccessToken(newTokens.accessToken);
            return axios(error.config);
          } catch (refreshError) {
            console.error('Error refreshing token:', refreshError);
            setAccessToken(null);
            setRefreshToken(null);
            Cookies.remove('accessToken');
            Cookies.remove('refreshToken');
            navigate('/login');
            return Promise.reject(refreshError);
          }
        }
        return Promise.reject(error);
      }
    );
    return () => {
      axios.interceptors.response.eject(responseInterceptor);
    };
  }, [accessToken, refreshToken, navigate]);

  const refreshAccessToken = async () => {
    try {
      const response = await axios.post('/api/user/refresh-token', {
        refreshToken,
      });
      console.log('Access token refreshed:', response.data);

      const { accessToken: newAccessToken, refreshToken: newRefreshToken } = response.data;
      Cookies.set('accessToken', newAccessToken, { expires: 1 / 24 }); 
      Cookies.set('refreshToken', newRefreshToken, { expires: 7 }); 

      setAccessToken(newAccessToken);
      setRefreshToken(newRefreshToken);

      return { accessToken: newAccessToken, refreshToken: newRefreshToken };
    } catch (error) {
      console.error('Error refreshing access token:', error);
      Cookies.remove('accessToken');
      Cookies.remove('refreshToken');
      navigate('/login');
      throw error;
    }
  };

  return (
    <AuthContext.Provider value={{ accessToken, setAccessToken, refreshToken, setRefreshToken }}>
      {children}
    </AuthContext.Provider>
  );
};
