import { styled } from '@mui/material/styles'
import Box from '@mui/material/Box'

const DrawerHeaderStyled = styled(Box, {
  shouldForwardProp: (prop) => prop !== 'open',
})(({ theme, open }) => ({
  ...theme.mixins.toolbar,
  display: 'flex',
  alignItems: 'center',
  justifyContent: open ? 'flex-start' : 'center',
  minHeight: open ? 132 : 68,
  paddingLeft: theme.spacing(open ? 2.5 : 0),
  borderBottom: `1px solid ${theme.palette.divider}`,
  backgroundColor: theme.palette.background.paper,
}))

export default DrawerHeaderStyled
