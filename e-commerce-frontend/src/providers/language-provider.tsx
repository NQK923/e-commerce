'use client';

import React, { createContext, useContext, useEffect, useState } from 'react';
import { en } from '../i18n/locales/en';
import { vi } from '../i18n/locales/vi';

type Language = 'en' | 'vi';
type Translations = typeof en;

interface LanguageContextType {
  language: Language;
  t: Translations;
  setLanguage: (lang: Language) => void;
}

const LanguageContext = createContext<LanguageContextType | undefined>(undefined);

const STORAGE_KEY = 'ecommerce_lang_v2';
const COOKIE_KEY = 'ecommerce_lang_v2';

const resolveStoredLanguage = (): Language | null => {
  if (typeof window === 'undefined') return null;
  try {
    const savedLang = window.localStorage.getItem(STORAGE_KEY);
    if (savedLang === 'en' || savedLang === 'vi') {
      return savedLang;
    }

    const cookieLang = document.cookie
      .split('; ')
      .find((row) => row.startsWith(`${COOKIE_KEY}=`))
      ?.split('=')[1];

    if (cookieLang === 'en' || cookieLang === 'vi') {
      return cookieLang;
    }
  } catch (e) {
    console.error("Failed to read language from storage", e);
  }
  return null;
};

const persistLanguage = (lang: Language) => {
  try {
    window.localStorage.setItem(STORAGE_KEY, lang);
  } catch (e) {
    console.error("Failed to save language to storage", e);
  }
  try {
    document.cookie = `${COOKIE_KEY}=${lang}; path=/; max-age=31536000`;
  } catch (e) {
    console.error("Failed to persist language cookie", e);
  }
  if (typeof document !== 'undefined') {
    document.documentElement.lang = lang;
  }
};

type LanguageProviderProps = {
  children: React.ReactNode;
  initialLanguage?: Language;
};

export function LanguageProvider({ children, initialLanguage = 'vi' }: LanguageProviderProps) {
  const [language, setLanguageState] = useState<Language>(() => resolveStoredLanguage() ?? initialLanguage);

  useEffect(() => {
    const stored = resolveStoredLanguage();
    if (stored && stored !== language) {
      setLanguageState(stored);
      return;
    }
    persistLanguage(language);
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const setLanguage = (lang: Language) => {
    setLanguageState(lang);
  };

  useEffect(() => {
    persistLanguage(language);
  }, [language]);

  const t = language === 'vi' ? vi : en;

  return (
    <LanguageContext.Provider value={{ language, t, setLanguage }}>
      {children}
    </LanguageContext.Provider>
  );
}

export function useTranslation() {
  const context = useContext(LanguageContext);
  if (context === undefined) {
    throw new Error('useTranslation must be used within a LanguageProvider');
  }
  return context;
}
