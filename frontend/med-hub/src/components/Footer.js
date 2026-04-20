import React from "react";
import styles from "./Footer.module.css";

function Footer() {
    const year = new Date().getFullYear();

    return (
        <footer className={styles.footer} role="contentinfo">
            <span>{`© ${year} MedHub. All rights reserved.`}</span>
        </footer>
    );
}

export default Footer;
