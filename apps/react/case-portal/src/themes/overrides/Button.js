// ==============================|| OVERRIDES - BUTTON ||============================== //

export default function Button(theme) {
  const disabledStyle = {
    '&.Mui-disabled': {
      backgroundColor: theme.palette.grey[200],
    },
  }

  return {
    MuiButton: {
      defaultProps: {
        disableElevation: true,
      },
      styleOverrides: {
        root: {
          borderRadius: 6,
          fontWeight: 600,
          letterSpacing: 0,
        },
        contained: {
          ...disabledStyle,
        },
        outlined: {
          ...disabledStyle,
          borderColor: theme.palette.primary.main,
        },
      },
    },
  }
}
